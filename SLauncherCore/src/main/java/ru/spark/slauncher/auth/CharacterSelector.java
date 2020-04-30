package ru.spark.slauncher.auth;

import ru.spark.slauncher.auth.yggdrasil.GameProfile;
import ru.spark.slauncher.auth.yggdrasil.YggdrasilService;

import java.util.List;

/**
 * This interface is for your application to open a GUI for user to choose the character
 * when a having-multi-character yggdrasil account is being logging in..
 */
public interface CharacterSelector {

    /**
     * Select one of {@code names} GameProfiles to login.
     *
     * @param names available game profiles.
     * @return your choice of game profile.
     * @throws NoSelectedCharacterException if cannot select any character may because user close the selection window or cancel the selection.
     */
    GameProfile select(YggdrasilService yggdrasilService, List<GameProfile> names) throws NoSelectedCharacterException;

}
