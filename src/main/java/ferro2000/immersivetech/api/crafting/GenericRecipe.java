package ferro2000.immersivetech.api.crafting;

import java.util.ArrayList;

public abstract class GenericRecipe {
	public String recipeName;
	protected boolean isValid;

	public boolean isValid() {
		return isValid;
	}

	@SuppressWarnings("serial")
	public static class RecipeList <T extends GenericRecipe> extends ArrayList <T> {
		@Override
		public boolean add(T t) {
			t.isValid = true;
			return super.add(t);
		}

		@Override
		public boolean remove(Object o) {
			if(o instanceof GenericRecipe) ((GenericRecipe)o).isValid = false;
			return super.remove(o);
		}
	}

}