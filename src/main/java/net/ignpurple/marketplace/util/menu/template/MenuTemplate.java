package net.ignpurple.marketplace.util.menu.template;

import com.google.common.collect.Lists;
import net.ignpurple.marketplace.util.config.annotation.Section;

import java.util.ArrayList;
import java.util.List;

@Section
public class MenuTemplate {
    private String title;
    private List<String> menuDesign;
    private List<MenuButton> buttons;

    public MenuTemplate(Builder builder) {
        this.title = builder.title;
        this.menuDesign = builder.menuDesign;
        this.buttons = builder.buttons;
    }

    protected MenuTemplate() {}

    public String getTitle() {
        return this.title;
    }

    public List<String> getMenuDesign() {
        return this.menuDesign;
    }

    public MenuButton getButtonBySymbol(String symbol) {
        return this.buttons.stream().filter((button) -> button.getIdentifier().equals(symbol)).findFirst().orElse(null);
    }

    public MenuButton getButtonByName(String name) {
        return this.buttons.stream().filter((button) -> button.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public List<MenuButton> getButtons() {
        return this.buttons;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String title;
        private List<String> menuDesign;
        private final List<MenuButton> buttons = new ArrayList<>();

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder design(String... rows) {
            this.menuDesign = Lists.newArrayList(rows);
            return this;
        }

        public Builder button(MenuButton button) {
            this.buttons.add(button);
            return this;
        }

        public MenuTemplate build() {
            return new MenuTemplate(this);
        }
    }
}
