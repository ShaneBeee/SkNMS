package com.shanebeestudios.nms.elements.other.structures;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.LoopSection;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.sections.SecConditional;
import com.shanebeestudios.nms.api.skript.RegistrationSection;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.structure.Structure;

@Name("Registry Registration")
@Description("This structure is used for registering new registry entries such as custom biomes and enchantments.")
@Since("1.1.0")
public class StructRegistryRegistration extends Structure {

    private static final Priority PRIORITY = new Priority(201);

    static {
        Skript.registerStructure(StructRegistryRegistration.class, "registry registration");
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult, @Nullable EntryContainer entryContainer) {
        if (entryContainer == null) return false;
        getParser().setCurrentStructure(this);

        for (Node node : entryContainer.getSource()) {
            if (node instanceof SectionNode sectionNode) {
                String key = sectionNode.getKey();
                if (key == null) continue;

                Section something = Section.parse(key, "Invalid section", sectionNode, null);
                if (something instanceof RegistrationSection || something instanceof LoopSection || something instanceof SecConditional) {
                    // Walk at parse time to ensure custom registry entries can be used in other code
                    // Allow loops and conditions to be executed as well
                    Section.walk(something, new RegistrationSection.RegistrationEvent());
                } else {
                    // All other sections will be ignored
                    Skript.error("Invalid section entry '" + key + "' cannot be used in a registration structure.");
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean load() {
        return true;
    }

    @Override
    public Priority getPriority() {
        return PRIORITY;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "registry registration";
    }

}
