/*
 * Copyright 2017 FBK/CREATE-NET
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.createnet.raptor.cli;

import com.beust.jcommander.JCommander;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.createnet.raptor.config.ConfigurationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.createnet.raptor.cli.command.Command;

/**
 *
 * @author Luca Capra <lcapra@fbk.eu>
 */
public class Runner {

    static {
        // initialize logback config path
        String configPath = ConfigurationLoader.getLoader().getConfigPath();
        File filePath = new File(configPath + "/logback.xml");
        if (filePath.exists()) {
            System.setProperty("logback.configurationFile", filePath.getAbsolutePath());
        } else {
            System.out.println("Logback configuration file does not esists at " + filePath.getAbsolutePath());
        }
    }

    static final private Logger logger = LoggerFactory.getLogger(Runner.class);
    protected JCommander cmd;

    // add here commands!
    final protected Class[] availCommands = new Class[]{};

    final protected Map<String, Command> commands = new HashMap();

    public static void main(String[] args) {
        final Runner app = new Runner();
        app.initialize(args);
        app.run();
    }

    private void initialize(String[] args) {

        cmd = new JCommander(this);

        for (Class availCommand : availCommands) {
            try {
                Command c = (Command) availCommand.newInstance();
                logger.debug("Added command {}", c.getName());
                commands.put(c.getName(), c);
                cmd.addCommand(c.getName(), c);
            } catch (InstantiationException | IllegalAccessException ex) {
                throw new Command.CommandException(ex);
            }
        }

        cmd.parse(args);
    }

    private void run() {

        String subcommand = cmd.getParsedCommand();

        if (commands.containsKey(subcommand)) {

            try {
                logger.info("Running command {}", subcommand);
                commands.get(subcommand).run();
            } catch (Command.CommandException ex) {
                logger.error("Execption running command: {}", ex.getMessage());
                throw new RuntimeException(ex);
            }
            return;
        }

        cmd.usage();
    }

    // from http://stackoverflow.com/a/24064448/833499 
    public static boolean isRoot() {

        try {
            String osName = System.getProperty("os.name").toLowerCase();
            String className = null;

            if (osName.contains("windows")) {
                className = "com.sun.security.auth.module.NTSystem";
            } else if (osName.contains("linux")) {
                className = "com.sun.security.auth.module.UnixSystem";
            } else if (osName.contains("solaris") || osName.contains("sunos")) {
                className = "com.sun.security.auth.module.SolarisSystem";
            }

            if (className != null) {
                Class<?> c = Class.forName(className);
                Method method = c.getDeclaredMethod("getUsername");
                Object o = c.newInstance();
                String name = (String) method.invoke(o);

                return name.equals("root") || name.equals("Administrator");
            }

            return false;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void checkUser() {
        try {
            if (!Runner.isRoot()) {
                logger.debug("Current user is not root");
            }
        } catch (Exception e) {
            logger.error("Cannot get current username: ", e.getMessage());
        }
    }

}
