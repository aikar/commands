/*
 * Copyright (c) 2016-2017 Daniel Ennis (Aikar) - MIT License
 *
 *  Permission is hereby granted, free of charge, to any person obtaining
 *  a copy of this software and associated documentation files (the
 *  "Software"), to deal in the Software without restriction, including
 *  without limitation the rights to use, copy, modify, merge, publish,
 *  distribute, sublicense, and/or sell copies of the Software, and to
 *  permit persons to whom the Software is furnished to do so, subject to
 *  the following conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 *  LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 *  OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 *  WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package co.aikar.commands;

import co.aikar.commands.annotation.Optional;
import co.aikar.commands.contexts.CommandResultSupplier;
import co.aikar.commands.contexts.OnlinePlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;

@SuppressWarnings("WeakerAccess")
public class SpongeCommandContexts extends CommandContexts<SpongeCommandExecutionContext> {

    public SpongeCommandContexts(final SpongeCommandManager manager) {
        super(manager);

        registerIssuerOnlyContext(CommandResultSupplier.class, c -> new CommandResultSupplier());
        registerContext(OnlinePlayer.class, c -> getOnlinePlayer(c.getIssuer(), c.popFirstArg(), c.hasAnnotation(Optional.class)));


    }

    @Nullable
    OnlinePlayer getOnlinePlayer(SpongeCommandIssuer issuer, String lookup, boolean allowMissing) throws InvalidCommandArgument {
        CommandSource sender = issuer.getIssuer();
        Player player = ACFSpongeUtil.findPlayerSmart(issuer, lookup);
        //noinspection Duplicates
        if (player == null) {
            if (allowMissing) {
                return null;
            }
            this.manager.sendMessage(sender, MessageType.ERROR, MessageKeys.COULD_NOT_FIND_PLAYER, "{search}", lookup);
            throw new InvalidCommandArgument(false);
        }
        return new OnlinePlayer(player);
    }
}
