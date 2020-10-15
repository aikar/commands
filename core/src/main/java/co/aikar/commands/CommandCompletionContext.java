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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CommandCompletionContext<I extends CommandIssuer> {
    private final RegisteredCommand command;
    protected final I issuer;
    private final String input;
    private final String config;
    private final Map<String, String> configs = new HashMap<>();
    private final List<String> args;

    CommandCompletionContext(RegisteredCommand command, I issuer, String input, String config, String[] args) {
        this.command = command;
        this.issuer = issuer;
        this.input = input;
        if (config != null) {
            String[] configs = ACFPatterns.COMMA.split(config);
            for (String conf : configs) {
                String[] confsplit = ACFPatterns.EQUALS.split(conf, 2);
                this.configs.put(confsplit[0].toLowerCase(Locale.ENGLISH), confsplit.length > 1 ? confsplit[1] : null);
            }
            this.config = configs[0];
        } else {
            this.config = null;
        }

        this.args = Arrays.asList(args);
    }

    public Map<String, String> getConfigs() {
        return configs;
    }

    public String getConfig(String key) {
        return getConfig(key, null);
    }

    public String getConfig(String key, String def) {
        return this.configs.getOrDefault(key.toLowerCase(Locale.ENGLISH), def);
    }

    public boolean hasConfig(String key) {
        return this.configs.containsKey(key.toLowerCase(Locale.ENGLISH));
    }

    public <T> T getContextValue(Class<? extends T> clazz) throws InvalidCommandArgument {
        return getContextValue(clazz, null);
    }

    public <T> T getContextValue(Class<? extends T> clazz, Integer paramIdx) throws InvalidCommandArgument {
        String name = null;
        if (paramIdx != null) {
            if (paramIdx >= command.parameters.length) {
                throw new IllegalArgumentException("Param index is higher than number of parameters");
            }
            CommandParameter param = command.parameters[paramIdx];
            Class<?> paramType = param.getType();
            if (!clazz.isAssignableFrom(paramType)) {
                throw new IllegalArgumentException(param.getName() + ":" + paramType.getName() + " can not satisfy " + clazz.getName());
            }
            name = param.getName();
        } else {
            CommandParameter[] parameters = command.parameters;
            for (int i = 0; i < parameters.length; i++) {
                final CommandParameter parameter = parameters[i];
                if (clazz.isAssignableFrom(parameter.getType())) {
                    paramIdx = i;
                    name = parameter.getName();
                    break;
                }
            }
            if (paramIdx == null) {
                throw new IllegalStateException("Can not find any parameter that can satisfy " + clazz.getName());
            }
        }
        return getContextValueByName(clazz, name);
    }

    public <T> T getContextValueByName(Class<? extends T> clazz, String name) throws InvalidCommandArgument {
        //noinspection unchecked
        Map<String, Object> resolved = command.resolveContexts(issuer, args, name);
        if (resolved == null || !resolved.containsKey(name)) {
            ACFUtil.sneaky(new CommandCompletionTextLookupException());
        }

        //noinspection unchecked
        return (T) resolved.get(name);
    }

    public CommandIssuer getIssuer() {
        return issuer;
    }

    public String getInput() {
        return input;
    }

    public String getConfig() {
        return config;
    }

    public boolean isAsync() {
        return CommandManager.getCurrentCommandOperationContext().isAsync();
    }
}
