package co.aikar.commands;

import java.util.List;
import java.util.Map;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

public class VelocityCommandExecutionContext extends CommandExecutionContext<VelocityCommandExecutionContext, VelocityCommandIssuer> {

    VelocityCommandExecutionContext(RegisteredCommand cmd, CommandParameter param, VelocityCommandIssuer sender, List<String> args, int index, Map<String, Object> passedArgs) {
        super(cmd, param, sender, args, index, passedArgs);
    }

    public CommandSource getSender() {
        return this.issuer.getIssuer();
    }

    public Player getPlayer() {
        return this.issuer.getPlayer();
    }
}
