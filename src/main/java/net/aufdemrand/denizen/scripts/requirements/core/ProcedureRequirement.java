package net.aufdemrand.denizen.scripts.requirements.core;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.aufdemrand.denizen.scripts.requirements.RequirementsContext;
import net.aufdemrand.denizen.utilities.debugging.dB;

import java.util.List;

public class ProcedureRequirement extends AbstractRequirement {


    @Override
    public boolean check(RequirementsContext context, List<String> args) throws RequirementCheckException {

        // Deprecated
        dB.log("This requirement is deprecated! Use instead: valueof <proc[name_of_procedure_script]>");

        return false;
    }
}
