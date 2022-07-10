//TODO übernehmen
package de.keksuccino.fancymenu.menu.fancy.item.visibilityrequirements.requirements;

import de.keksuccino.fancymenu.api.visibilityrequirements.VisibilityRequirement;
import de.keksuccino.fancymenu.menu.variables.VariableHandler;
import de.keksuccino.konkrete.input.CharacterFilter;
import de.keksuccino.konkrete.input.StringUtils;
import de.keksuccino.konkrete.localization.Locals;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class IsVariableValueVisibilityRequirement extends VisibilityRequirement {

    public IsVariableValueVisibilityRequirement() {
        super("fancymenu_visibility_requirement_is_variable_value");
    }

    @Override
    public boolean hasValue() {
        return true;
    }

    @Override
    public boolean isRequirementMet(@Nullable String value) {

        if (value != null) {
            if (value.contains(":")) {
                String name = value.split("[:]", 2)[0];
                String val = value.split("[:]", 2)[1];
                String storedVal = VariableHandler.getVariable(name);
                if (storedVal != null) {
                    return val.equals(storedVal);
                }
            }
        }

        return false;

    }

    @Override
    public String getDisplayName() {
        return Locals.localize("fancymenu.helper.visibilityrequirement.is_variable_value");
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList(StringUtils.splitLines(Locals.localize("fancymenu.helper.visibilityrequirement.is_variable_value.desc"), "%n%"));
    }

    @Override
    public String getValueDisplayName() {
        return Locals.localize("fancymenu.helper.visibilityrequirement.is_variable_value.value.desc");
    }

    @Override
    public String getValuePreset() {
        return "<variable_name>:<value_to_check_for>";
    }

    @Override
    public CharacterFilter getValueInputFieldFilter() {
        return null;
    }

}