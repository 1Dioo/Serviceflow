package com.serviceflow.service;

import com.serviceflow.model.User;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Proteção contra tentativas repetidas de login (em memória).
 *
 * Regras por canal:
 *  - USER:  5 falhas seguidas → bloqueio de 30 s, dobrando a cada novo
 *           bloqueio, até 15 min.
 *  - ADMIN: 3 falhas seguidas → bloqueio de 60 s, dobrando a cada novo
 *           bloqueio, até 30 min.
 *
 * Os contadores são independentes por canal e por usuário normalizado
 * (sem espaços nas pontas e em minúsculas). Usuários inexistentes são
 * contados da mesma forma, para não revelar quais contas existem.
 * Durante o bloqueio nenhuma senha é verificada, nem a correta.
 * Um login bem-sucedido zera o contador e a progressão daquele usuário.
 *
 * Limitação conhecida: por ser em memória, os contadores são zerados
 * quando a aplicação é reiniciada.
 */
public final class LoginAttemptService {

    public enum Channel {
        USER(5, Duration.ofSeconds(30), Duration.ofMinutes(15)),
        ADMIN(3, Duration.ofSeconds(60), Duration.ofMinutes(30));

        private final int maxFailures;
        private final Duration firstLockout;
        private final Duration maxLockout;

        Channel(int maxFailures, Duration firstLockout, Duration maxLockout) {
            this.maxFailures = maxFailures;
            this.firstLockout = firstLockout;
            this.maxLockout = maxLockout;
        }

        public int getMaxFailures() {
            return maxFailures;
        }

        public Duration getFirstLockout() {
            return firstLockout;
        }

        public Duration getMaxLockout() {
            return maxLockout;
        }
    }

    public enum Status {
        SUCCESS,
        INVALID,
        LOCKED
    }

    /**
     * Resultado de uma tentativa.
     *
     * @param status        SUCCESS, INVALID (credenciais inválidas) ou LOCKED
     * @param user          usuário autenticado (somente em SUCCESS)
     * @param lockRemaining tempo restante de bloqueio (somente em LOCKED)
     */
    public record Result(Status status, User user, Duration lockRemaining) {
    }

    private static final class AttemptState {
        private int consecutiveFailures;
        private int lockouts;
        private Instant lockedUntil;
    }

    private static final LoginAttemptService SHARED =
        new LoginAttemptService(Clock.systemUTC());

    private final Clock clock;

    private final Map<String, AttemptState> states = new HashMap<>();

    public LoginAttemptService(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
    }

    /** Instância usada pelas telas de login da aplicação. */
    public static LoginAttemptService shared() {
        return SHARED;
    }

    /**
     * Executa uma tentativa de login respeitando o bloqueio do canal.
     *
     * @param authenticator autenticação real (ex.: UserRepository::authenticateUser);
     *                      não é chamada enquanto o usuário estiver bloqueado
     */
    public synchronized Result attempt(Channel channel, String username,
                                       Supplier<User> authenticator) {
        Duration remaining = lockRemaining(channel, username);

        if (!remaining.isZero()) {
            return new Result(Status.LOCKED, null, remaining);
        }

        User user = authenticator.get();

        if (user != null) {
            recordSuccess(channel, username);
            return new Result(Status.SUCCESS, user, Duration.ZERO);
        }

        recordFailure(channel, username);

        remaining = lockRemaining(channel, username);

        return remaining.isZero()
            ? new Result(Status.INVALID, null, Duration.ZERO)
            : new Result(Status.LOCKED, null, remaining);
    }

    /** Tempo restante de bloqueio, ou Duration.ZERO se não estiver bloqueado. */
    public synchronized Duration lockRemaining(Channel channel, String username) {
        AttemptState state = states.get(key(channel, username));

        if (state == null || state.lockedUntil == null) {
            return Duration.ZERO;
        }

        Duration remaining = Duration.between(clock.instant(), state.lockedUntil);

        return remaining.isNegative() || remaining.isZero()
            ? Duration.ZERO
            : remaining;
    }

    public synchronized void recordFailure(Channel channel, String username) {
        AttemptState state = states.computeIfAbsent(
            key(channel, username), k -> new AttemptState()
        );

        state.consecutiveFailures++;

        if (state.consecutiveFailures >= channel.getMaxFailures()) {
            state.lockouts++;
            state.consecutiveFailures = 0;
            state.lockedUntil = clock.instant().plus(
                lockoutDuration(channel, state.lockouts)
            );
        }
    }

    public synchronized void recordSuccess(Channel channel, String username) {
        states.remove(key(channel, username));
    }

    /**
     * Duração do n-ésimo bloqueio: primeiro bloqueio × 2^(n-1),
     * limitado ao máximo do canal.
     */
    static Duration lockoutDuration(Channel channel, int lockoutNumber) {
        Duration duration = channel.getFirstLockout();

        for (int i = 1; i < lockoutNumber; i++) {
            duration = duration.multipliedBy(2);

            if (duration.compareTo(channel.getMaxLockout()) >= 0) {
                return channel.getMaxLockout();
            }
        }

        return duration.compareTo(channel.getMaxLockout()) > 0
            ? channel.getMaxLockout()
            : duration;
    }

    /**
     * Texto do tempo restante para a interface, arredondado para cima
     * (ex.: "30 segundos", "1 minuto e 30 segundos", "15 minutos").
     */
    public static String formatRemaining(Duration remaining) {
        long totalSeconds = Math.max(1, (remaining.toMillis() + 999) / 1000);
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        String minutesText = minutes == 1 ? "1 minuto" : minutes + " minutos";
        String secondsText = seconds == 1 ? "1 segundo" : seconds + " segundos";

        if (minutes == 0) {
            return secondsText;
        }

        return seconds == 0 ? minutesText : minutesText + " e " + secondsText;
    }

    /** Mensagem padrão de bloqueio exibida nas telas de login. */
    public static String lockMessage(Duration remaining) {
        return "Muitas tentativas. Tente novamente em "
            + formatRemaining(remaining) + ".";
    }

    private static String key(Channel channel, String username) {
        String normalized = username == null
            ? ""
            : username.trim().toLowerCase(Locale.ROOT);

        return channel.name() + ":" + normalized;
    }
}
