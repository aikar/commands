package co.aikar.commands;

import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

public class JDACommandExecutionContext extends CommandExecutionContext<JDACommandExecutionContext,JDACommandEvent> {
    JDACommandExecutionContext(RegisteredCommand cmd, Parameter param, JDACommandEvent sender, List<String> args, int index, Map<String, Object> passedArgs) {
        super(cmd, param, sender, args, index, passedArgs);
    }
}
