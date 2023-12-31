package cc.mewcraft.mewhelp;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class HelpTopicArgument extends CommandArgument<CommandSender, HelpTopic> {
    public HelpTopicArgument(
            boolean required,
            String name,
            String defaultValue,
            @Nullable BiFunction<CommandContext<CommandSender>, String, List<String>> suggestionsProvider,
            ArgumentDescription defaultDescription
    ) {
        super(required, name, new Parser(), defaultValue, HelpTopic.class, suggestionsProvider, defaultDescription);
    }

    public static HelpTopicArgument of(final String name) {
        return builder(name).build();
    }

    public static HelpTopicArgument optional(final String name) {
        return builder(name).asOptional().build();
    }

    public static HelpTopicArgument.Builder builder(final String name) {
        return new HelpTopicArgument.Builder(name);
    }

    public static final class Parser implements ArgumentParser<CommandSender, HelpTopic> {
        @Override
        public ArgumentParseResult<HelpTopic> parse(
                final CommandContext<CommandSender> commandContext,
                final Queue<String> inputQueue
        ) {
            @Nullable String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(HelpTopicArgument.Parser.class, commandContext));
            }

            CommandSender sender = commandContext.getSender();

            @Nullable HelpTopic target = MewHelpPlugin.getInstance().helpTopics().topic(input);
            if (target != null) {
                inputQueue.remove();
                return ArgumentParseResult.success(target);
            }

            return ArgumentParseResult.failure(
                    new IllegalArgumentException(
                            MewHelpPlugin.getInstance().translations()
                                    .of("msg_use_tab_for_help")
                                    .replace("command", commandContext.getRawInput().get(0))
                                    .locale(sender)
                                    .plain()
                    )
            );
        }

        @Override
        public List<String> suggestions(
                final CommandContext<CommandSender> commandContext,
                final String input
        ) {
            return MewHelpPlugin.getInstance()
                    .helpTopics().topics()
                    .stream()
                    .filter(topic -> commandContext.getSender().hasPermission("mewhelp.help." + topic))
                    .toList();
        }
    }

    public static final class Builder extends CommandArgument.TypedBuilder<CommandSender, HelpTopic, HelpTopicArgument.Builder> {
        private Builder(final String name) {
            super(HelpTopic.class, name);
        }

        @Override
        public HelpTopicArgument build() {
            return new HelpTopicArgument(
                    this.isRequired(),
                    this.getName(),
                    this.getDefaultValue(),
                    this.getSuggestionsProvider(),
                    this.getDefaultDescription()
            );
        }
    }

}
