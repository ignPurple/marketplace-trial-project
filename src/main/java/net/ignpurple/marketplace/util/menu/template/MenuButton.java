package net.ignpurple.marketplace.util.menu.template;

import net.ignpurple.marketplace.util.config.annotation.Section;
import net.ignpurple.marketplace.util.config.annotation.SectionName;
import org.bukkit.inventory.ItemStack;

@Section
public class MenuButton {
    @SectionName
    private String name;
    private String identifier;
    private ItemStack item;

    public MenuButton(Builder builder) {
        this.name = builder.name;
        this.identifier = builder.identifier;
        this.item = builder.item;
    }

    protected MenuButton() {}

    public String getName() {
        return this.name;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public ItemStack getItem() {
        return this.item;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String identifier;
        private ItemStack item;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder identifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        public Builder item(ItemStack item) {
            this.item = item;
            return this;
        }

        public MenuButton build() {
            return new MenuButton(this);
        }
    }
}
