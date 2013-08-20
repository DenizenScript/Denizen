package net.aufdemrand.denizen.npc.traits;

import java.util.ArrayList;
import java.util.List;

import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Owner;

public class CreatorTrait extends Trait {
    
    String creator = null;
    List<Integer> created = new ArrayList<Integer>();
    
    @Override
    public void onAttach() {
        creator = npc.getTrait(Owner.class).getOwner();
    }
    
    public String getCreator() {
        return creator;
    }
    
    public List getCreated() {
        return created;
    }
    
    protected CreatorTrait() {
        super("Creator");
    }

}
