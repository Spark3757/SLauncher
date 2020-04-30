package ru.spark.slauncher.ui;

import ru.spark.slauncher.auth.Account;
import ru.spark.slauncher.auth.AuthInfo;
import ru.spark.slauncher.auth.AuthenticationException;
import ru.spark.slauncher.auth.yggdrasil.YggdrasilAccount;
import ru.spark.slauncher.ui.account.AccountLoginPane;

import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static ru.spark.slauncher.ui.FXUtils.runInFX;

public final class DialogController {

    public static AuthInfo logIn(Account account) throws CancellationException, AuthenticationException, InterruptedException {
        if (account instanceof YggdrasilAccount) {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<AuthInfo> res = new AtomicReference<>(null);
            runInFX(() -> {
                AccountLoginPane pane = new AccountLoginPane(account, it -> {
                    res.set(it);
                    latch.countDown();
                }, latch::countDown);
                Controllers.dialog(pane);
            });
            latch.await();
            return Optional.ofNullable(res.get()).orElseThrow(CancellationException::new);
        }
        return account.logIn();
    }
}
