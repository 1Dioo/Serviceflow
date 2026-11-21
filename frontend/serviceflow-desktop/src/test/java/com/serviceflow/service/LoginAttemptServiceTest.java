package com.serviceflow.service;

import com.serviceflow.model.User;
import com.serviceflow.service.LoginAttemptService.Channel;
import com.serviceflow.service.LoginAttemptService.Result;
import com.serviceflow.service.LoginAttemptService.Status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("LoginAttemptService - limite progressivo de tentativas")
class LoginAttemptServiceTest {

    /** Relógio controlado pelo teste. */
    static final class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-01-01T10:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    private static final Supplier<User> WRONG = () -> null;

    private MutableClock clock;
    private LoginAttemptService service;

    @BeforeEach
    void setUp() {
        clock = new MutableClock();
        service = new LoginAttemptService(clock);
    }

    private void fail(Channel channel, String username, int times) {
        for (int i = 0; i < times; i++) {
            service.attempt(channel, username, WRONG);
        }
    }

    private static User someUser() {
        return new User("Teste", "teste", "senhaSegura123", User.Role.USER);
    }

    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Canal USER (5 falhas, 30 s, até 15 min)")
    class UserChannel {

        @Test
        @DisplayName("4 falhas não bloqueiam; a 5ª bloqueia por 30 s")
        void locksOnFifthFailure() {
            for (int i = 1; i <= 4; i++) {
                assertEquals(Status.INVALID, service.attempt(Channel.USER, "maria", WRONG).status(),
                    "falha " + i);
            }

            Result fifth = service.attempt(Channel.USER, "maria", WRONG);

            assertEquals(Status.LOCKED, fifth.status());
            assertEquals(Duration.ofSeconds(30), fifth.lockRemaining());
        }

        @Test
        @DisplayName("durante o bloqueio, nem a senha correta é verificada")
        void correctPasswordBlockedDuringLock() {
            fail(Channel.USER, "maria", 5);
            AtomicInteger calls = new AtomicInteger();
            User valid = someUser();

            Result result = service.attempt(Channel.USER, "maria", () -> {
                calls.incrementAndGet();
                return valid;
            });

            assertEquals(Status.LOCKED, result.status());
            assertNull(result.user());
            assertEquals(0, calls.get(), "a autenticação não pode ser chamada durante o bloqueio");
        }

        @Test
        @DisplayName("tempo restante diminui com o relógio")
        void remainingTimeDecreases() {
            fail(Channel.USER, "maria", 5);
            clock.advance(Duration.ofSeconds(12));

            assertEquals(Duration.ofSeconds(18), service.lockRemaining(Channel.USER, "maria"));
            assertEquals(Duration.ofSeconds(18),
                service.attempt(Channel.USER, "maria", WRONG).lockRemaining());
        }

        @Test
        @DisplayName("tentativas durante o bloqueio não prolongam o bloqueio")
        void attemptsDuringLockDoNotExtend() {
            fail(Channel.USER, "maria", 5);
            fail(Channel.USER, "maria", 20);

            clock.advance(Duration.ofSeconds(30));

            assertEquals(Duration.ZERO, service.lockRemaining(Channel.USER, "maria"));
        }

        @Test
        @DisplayName("o bloqueio expira e o login volta a funcionar")
        void lockExpires() {
            fail(Channel.USER, "maria", 5);
            clock.advance(Duration.ofSeconds(29));
            assertEquals(Status.LOCKED, service.attempt(Channel.USER, "maria", WRONG).status());

            clock.advance(Duration.ofSeconds(1));
            User valid = someUser();
            Result result = service.attempt(Channel.USER, "maria", () -> valid);

            assertEquals(Status.SUCCESS, result.status());
            assertSame(valid, result.user());
        }

        @Test
        @DisplayName("progressão: 30 s, 60 s, 2 min, 4 min, 8 min, 15 min (máximo)")
        void progressiveLockout() {
            List<Duration> expected = List.of(
                Duration.ofSeconds(30), Duration.ofSeconds(60), Duration.ofMinutes(2),
                Duration.ofMinutes(4), Duration.ofMinutes(8), Duration.ofMinutes(15),
                Duration.ofMinutes(15), Duration.ofMinutes(15)
            );

            for (Duration duration : expected) {
                fail(Channel.USER, "maria", 4);
                Result locked = service.attempt(Channel.USER, "maria", WRONG);

                assertEquals(Status.LOCKED, locked.status());
                assertEquals(duration, locked.lockRemaining());

                clock.advance(duration);
                assertEquals(Duration.ZERO, service.lockRemaining(Channel.USER, "maria"));
            }
        }

        @Test
        @DisplayName("após um bloqueio expirar, são necessárias 5 novas falhas")
        void needsFullCountAfterExpiry() {
            fail(Channel.USER, "maria", 5);
            clock.advance(Duration.ofSeconds(30));

            fail(Channel.USER, "maria", 4);

            assertEquals(Duration.ZERO, service.lockRemaining(Channel.USER, "maria"));
        }
    }

    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Canal ADMIN (3 falhas, 60 s, até 30 min)")
    class AdminChannel {

        @Test
        @DisplayName("2 falhas não bloqueiam; a 3ª bloqueia por 60 s")
        void locksOnThirdFailure() {
            assertEquals(Status.INVALID, service.attempt(Channel.ADMIN, "admin", WRONG).status());
            assertEquals(Status.INVALID, service.attempt(Channel.ADMIN, "admin", WRONG).status());

            Result third = service.attempt(Channel.ADMIN, "admin", WRONG);

            assertEquals(Status.LOCKED, third.status());
            assertEquals(Duration.ofSeconds(60), third.lockRemaining());
        }

        @Test
        @DisplayName("progressão: 1, 2, 4, 8, 16, 30 min (máximo)")
        void progressiveLockout() {
            List<Duration> expected = List.of(
                Duration.ofMinutes(1), Duration.ofMinutes(2), Duration.ofMinutes(4),
                Duration.ofMinutes(8), Duration.ofMinutes(16), Duration.ofMinutes(30),
                Duration.ofMinutes(30)
            );

            for (Duration duration : expected) {
                fail(Channel.ADMIN, "admin", 2);
                Result locked = service.attempt(Channel.ADMIN, "admin", WRONG);

                assertEquals(duration, locked.lockRemaining());
                clock.advance(duration);
            }
        }

        @Test
        @DisplayName("durante o bloqueio, nem a senha correta é verificada")
        void correctPasswordBlockedDuringLock() {
            fail(Channel.ADMIN, "admin", 3);
            AtomicInteger calls = new AtomicInteger();

            Result result = service.attempt(Channel.ADMIN, "admin", () -> {
                calls.incrementAndGet();
                return someUser();
            });

            assertEquals(Status.LOCKED, result.status());
            assertEquals(0, calls.get());
        }
    }

    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Reinicialização após sucesso")
    class Reset {

        @Test
        @DisplayName("sucesso zera o contador de falhas")
        void successResetsFailures() {
            fail(Channel.USER, "maria", 4);
            service.attempt(Channel.USER, "maria", LoginAttemptServiceTest::someUser);
            fail(Channel.USER, "maria", 4);

            assertEquals(Duration.ZERO, service.lockRemaining(Channel.USER, "maria"));
        }

        @Test
        @DisplayName("sucesso zera a progressão: o próximo bloqueio volta a 30 s")
        void successResetsProgression() {
            fail(Channel.USER, "maria", 5);
            clock.advance(Duration.ofSeconds(30));
            fail(Channel.USER, "maria", 5);
            assertEquals(Duration.ofSeconds(60), service.lockRemaining(Channel.USER, "maria"));
            clock.advance(Duration.ofSeconds(60));

            service.attempt(Channel.USER, "maria", LoginAttemptServiceTest::someUser);
            fail(Channel.USER, "maria", 5);

            assertEquals(Duration.ofSeconds(30), service.lockRemaining(Channel.USER, "maria"));
        }

        @Test
        @DisplayName("sucesso no canal ADMIN zera apenas o canal ADMIN")
        void successResetsOnlyItsChannel() {
            fail(Channel.USER, "admin", 4);
            fail(Channel.ADMIN, "admin", 2);

            service.attempt(Channel.ADMIN, "admin", LoginAttemptServiceTest::someUser);
            service.attempt(Channel.USER, "admin", WRONG);

            assertEquals(Duration.ofSeconds(30), service.lockRemaining(Channel.USER, "admin"));
        }
    }

    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Independência entre contas e canais")
    class Independence {

        @Test
        @DisplayName("bloqueio de um usuário não afeta outro")
        void independentUsers() {
            fail(Channel.USER, "maria", 5);

            assertEquals(Status.SUCCESS,
                service.attempt(Channel.USER, "joao", LoginAttemptServiceTest::someUser).status());
            assertEquals(Duration.ZERO, service.lockRemaining(Channel.USER, "joao"));
        }

        @Test
        @DisplayName("falhas de usuários diferentes não se somam")
        void failuresAreNotShared() {
            fail(Channel.USER, "maria", 4);
            fail(Channel.USER, "joao", 4);

            assertEquals(Duration.ZERO, service.lockRemaining(Channel.USER, "maria"));
            assertEquals(Duration.ZERO, service.lockRemaining(Channel.USER, "joao"));
        }

        @Test
        @DisplayName("bloqueio no canal USER não afeta o canal ADMIN e vice-versa")
        void independentChannels() {
            fail(Channel.USER, "admin", 5);
            assertEquals(Duration.ZERO, service.lockRemaining(Channel.ADMIN, "admin"));

            fail(Channel.ADMIN, "chefe", 3);
            assertEquals(Duration.ZERO, service.lockRemaining(Channel.USER, "chefe"));
        }

        @Test
        @DisplayName("usuário normalizado: maiúsculas e espaços contam como o mesmo usuário")
        void normalizesUsername() {
            service.attempt(Channel.USER, "Maria", WRONG);
            service.attempt(Channel.USER, " maria ", WRONG);
            service.attempt(Channel.USER, "MARIA", WRONG);
            service.attempt(Channel.USER, "maria", WRONG);
            Result fifth = service.attempt(Channel.USER, "  mArIa", WRONG);

            assertEquals(Status.LOCKED, fifth.status());
        }
    }

    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Usuários inexistentes")
    class UnknownUsers {

        @Test
        @DisplayName("são contados e bloqueados exatamente como contas existentes")
        void unknownUserTreatedLikeExisting() {
            String existing = "existente_" + UUID.randomUUID().toString().substring(0, 8);
            String unknown = "inexistente_" + UUID.randomUUID().toString().substring(0, 8);
            UserRepository.register("Existente", existing, "senhaSegura123");

            for (int i = 1; i <= 5; i++) {
                Result a = service.attempt(Channel.USER, existing,
                    () -> UserRepository.authenticateUser(existing, "senhaErrada123"));
                Result b = service.attempt(Channel.USER, unknown,
                    () -> UserRepository.authenticateUser(unknown, "senhaErrada123"));

                assertEquals(a.status(), b.status(), "tentativa " + i);
                assertEquals(a.lockRemaining(), b.lockRemaining(), "tentativa " + i);
            }

            assertEquals(Status.LOCKED,
                service.attempt(Channel.USER, unknown, LoginAttemptServiceTest::someUser).status());
        }

        @Test
        @DisplayName("integração: conta real bloqueada recusa a senha correta até expirar")
        void realAccountLockedThenUnlocked() {
            String username = "real_" + UUID.randomUUID().toString().substring(0, 8);
            UserRepository.register("Real", username, "senhaSegura123");

            for (int i = 0; i < 5; i++) {
                service.attempt(Channel.USER, username,
                    () -> UserRepository.authenticateUser(username, "senhaErrada123"));
            }

            Supplier<User> correct = () -> UserRepository.authenticateUser(username, "senhaSegura123");

            assertEquals(Status.LOCKED, service.attempt(Channel.USER, username, correct).status());

            clock.advance(Duration.ofSeconds(30));
            Result ok = service.attempt(Channel.USER, username, correct);

            assertEquals(Status.SUCCESS, ok.status());
            assertNotNull(ok.user());
        }
    }

    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Mensagem de tempo restante")
    class Messages {

        @Test
        @DisplayName("formata segundos e minutos em português")
        void formatsRemaining() {
            assertEquals("1 segundo", LoginAttemptService.formatRemaining(Duration.ofSeconds(1)));
            assertEquals("30 segundos", LoginAttemptService.formatRemaining(Duration.ofSeconds(30)));
            assertEquals("1 minuto", LoginAttemptService.formatRemaining(Duration.ofSeconds(60)));
            assertEquals("1 minuto e 30 segundos", LoginAttemptService.formatRemaining(Duration.ofSeconds(90)));
            assertEquals("2 minutos e 1 segundo", LoginAttemptService.formatRemaining(Duration.ofSeconds(121)));
            assertEquals("15 minutos", LoginAttemptService.formatRemaining(Duration.ofMinutes(15)));
        }

        @Test
        @DisplayName("arredonda para cima (nunca mostra 0 segundos)")
        void roundsUp() {
            assertEquals("30 segundos", LoginAttemptService.formatRemaining(Duration.ofMillis(29_100)));
            assertEquals("1 segundo", LoginAttemptService.formatRemaining(Duration.ofMillis(10)));
        }

        @Test
        @DisplayName("mensagem de bloqueio inclui o tempo restante")
        void lockMessage() {
            assertEquals("Muitas tentativas. Tente novamente em 30 segundos.",
                LoginAttemptService.lockMessage(Duration.ofSeconds(30)));
        }

        @Test
        @DisplayName("durações calculadas respeitam o máximo de cada canal")
        void lockoutDurationCaps() {
            assertEquals(Duration.ofMinutes(15), LoginAttemptService.lockoutDuration(Channel.USER, 50));
            assertEquals(Duration.ofMinutes(30), LoginAttemptService.lockoutDuration(Channel.ADMIN, 50));
            assertTrue(LoginAttemptService.lockoutDuration(Channel.USER, 1).equals(Duration.ofSeconds(30)));
        }
    }
}
