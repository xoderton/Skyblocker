package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// this widget shows a list of the owners while on your home island

public class IslandSelfWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Owners").formatted(Formatting.DARK_PURPLE,
            Formatting.BOLD);

    // matches an owner
    // group 1: player name, optionally offline time
    // ^\[\d*\] (?:\[[A-Za-z]+\] )?([A-Za-z0-9_() ]*)(?: .*)?$|^(.*)$
    private static final Pattern OWNER_PATTERN = Pattern
            .compile("^\\[\\d*\\] (?:\\[[A-Za-z]+\\] )?([A-Za-z0-9_() ]*)(?: .*)?$|^(.*)$");

    public IslandSelfWidget() {
        super(TITLE, Formatting.DARK_PURPLE.getColorValue());
    }

    @Override
    public void updateContent() {
        for (int i = 1; i < 20; i++) {
            Matcher m = PlayerListMgr.regexAt(i, OWNER_PATTERN);
            if (m == null) {
                break;
            }

            Text entry = (m.group(1) != null) ? Text.of(m.group(1)) : Text.of(m.group(2));
            this.addComponent(new PlainTextComponent(entry));
        }
    }

}
