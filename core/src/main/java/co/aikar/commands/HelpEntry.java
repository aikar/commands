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

import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.HelpSearchTags;

import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.Map;

public class HelpEntry {

    private final RegisteredCommand command;
    private final String searchTags;
    private int searchScore = 1;
    private Map<String, String> parameters = new LinkedHashMap<>();

    HelpEntry(RegisteredCommand command) {
        this.command = command;
        HelpSearchTags tagsAnno = command.method.getAnnotation(HelpSearchTags.class);
        this.searchTags = tagsAnno != null ? tagsAnno.value() : null;

        // search for parameter descriptions
        for (Parameter parameter : command.method.getParameters()) {
            Description description = parameter.getAnnotation(Description.class);
            if(description != null){
                parameters.put(parameter.getName(), description.value());
            }else{
                //TODO also add parameters with not description?
            }
        }
    }

    RegisteredCommand getRegisteredCommand() {
        return this.command;
    }

    public String getCommand(){
        return "/" + this.command.command;
    }

    public String getParameterSyntax(){
        return this.command.syntaxText;
    }

    public String getDescription(){
        return this.command.helpText;
    }

    public void setSearchScore(int searchScore) {
        this.searchScore = searchScore;
    }

    public boolean shouldShow() {
        return this.searchScore > 0;
    }

    public int getSearchScore() {
        return searchScore;
    }

    public String getSearchTags() {
        return searchTags;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }
}
