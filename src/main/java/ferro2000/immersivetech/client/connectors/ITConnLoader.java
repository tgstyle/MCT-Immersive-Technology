package ferro2000.immersivetech.client.connectors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import blusunrize.immersiveengineering.client.models.smart.ConnModelReal;
import ferro2000.immersivetech.ImmersiveTech;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.common.model.IModelState;

public class ITConnLoader implements ICustomModelLoader
{
	public static final String RESOURCE_LOCATION = "models/block/smartmodel/conns_";
	public static Map<String, ImmutableMap<String, String>> textureReplacements = new HashMap<>();
	public static Map<String, ResourceLocation> baseModels = new HashMap<>();
	static
	{
		
		baseModels.put("conn_timer", new ResourceLocation("immersivetech:block/connector/connectors_timer.obj.ie"));

	}

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager)
	{
		ConnModelReal.cache.clear();
	}

	@Override
	public boolean accepts(ResourceLocation modelLocation)
	{
		return modelLocation.getResourcePath().contains(RESOURCE_LOCATION);
	}

	@Override
	public IModel loadModel(ResourceLocation modelLocation) throws IOException
	{
		String resourcePath = modelLocation.getResourcePath();
		int pos = resourcePath.indexOf("conns_");
		if (pos >= 0)
		{
			pos += 6;// length of "conn_"
			String name = resourcePath.substring(pos);
			ResourceLocation r = baseModels.get(name);
			if (r != null)
			{
				if (textureReplacements.containsKey(name))
					return new ConnModelBase(r, textureReplacements.get(name));
				else
					return new ConnModelBase(r);
			}
		}
		return ModelLoaderRegistry.getMissingModel();
	}

	private class ConnModelBase implements IModel
	{
		ResourceLocation base;
		ImmutableMap<String, String> texReplace;

		public ConnModelBase(ResourceLocation b, ImmutableMap<String, String> t)
		{
			base = b;
			texReplace = t;
		}

		public ConnModelBase(ResourceLocation b)
		{
			this(b, ImmutableMap.of("", ""));
		}

		@Override
		public Collection<ResourceLocation> getDependencies()
		{
			return ImmutableList.of(base);
		}

		@Override
		public Collection<ResourceLocation> getTextures()
		{
			try
			{
				List<ResourceLocation> ret = new ArrayList<>(ModelLoaderRegistry.getModel(base).getTextures());
				for (String tex:texReplace.values())
					ret.add(new ResourceLocation(tex));
				ret.add(new ResourceLocation(ImmersiveTech.MODID.toLowerCase(Locale.ENGLISH) + ":blocks/wire"));
				return ret;
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		public IBakedModel bake(IModelState state, VertexFormat format,	Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
		{
			try
			{
				IModel model = ModelLoaderRegistry.getModel(base);
				if (model instanceof OBJModel)
				{
					model = model.retexture(texReplace);
					OBJModel obj = (OBJModel) model;
					model = obj.process(ImmutableMap.of("flip-v", "true"));
				}
				return new ConnModelReal(model.bake(state, format, bakedTextureGetter));
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		public IModelState getDefaultState()
		{
			return null;
		}

	}
}
