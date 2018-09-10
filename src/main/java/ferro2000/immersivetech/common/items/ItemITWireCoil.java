package ferro2000.immersivetech.common.items;

/*public class ItemITWireCoil extends ItemITBase implements IWireCoil {

	public ItemITWireCoil() {
		super("wirecoil", 64, "net");
	}

	@Override
	public WireType getWireType(ItemStack stack) {
		switch (stack.getItemDamage()) {
		default:
		case 0:
			return (WireType) ITWireType.NET;
		}
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if (stack.getTagCompound() != null && stack.getTagCompound().hasKey("linkingPos")) {
			int[] link = stack.getTagCompound().getIntArray("linkingPos");
			if (link != null && link.length > 3) {
				tooltip.add(I18n.format(Lib.DESC_INFO + "attachedToDim", link[1], link[2], link[3], link[0]));
			}
		}
	}

	// Copied from IE, due to a lack of other options. If IE's ItemWireCoil.onItemUse was factored out
	// into a static function, this wouldn't be necessary. (Well, it would also need a call out for
	// connection checks. See inside the function for details.)
	/*@Nonnull
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if(tileEntity instanceof IImmersiveConnectable && ((IImmersiveConnectable)tileEntity).canConnect())
		{
			ItemStack stack = player.getHeldItem(hand);
			TargetingInfo target = new TargetingInfo(side, hitX,hitY,hitZ);
			WireType wire = getWireType(stack);
			BlockPos masterPos = ((IImmersiveConnectable)tileEntity).getConnectionMaster(wire, target);
			Vec3i offset = pos.subtract(masterPos);
			tileEntity = world.getTileEntity(masterPos);
			if( !(tileEntity instanceof IImmersiveConnectable) || !((IImmersiveConnectable)tileEntity).canConnect())
				return EnumActionResult.PASS;

			//If there was some sort of call out here in the original function, we might not need to copy this function.
			if( !((IImmersiveConnectable)tileEntity).canConnectCable(wire, target, offset) || !canConnectCable(wire, tileEntity))
			{
				if (!world.isRemote)
					player.sendMessage(new TextComponentTranslation(Lib.CHAT_WARN+"wrongCable"));
				return EnumActionResult.FAIL;
			}

			if(!world.isRemote)
				if(!ItemNBTHelper.hasKey(stack, "linkingPos"))
				{
					ItemNBTHelper.setIntArray(stack, "linkingPos", new int[]{world.provider.getDimension(),masterPos.getX(),masterPos.getY(),masterPos.getZ(),
							offset.getX(), offset.getY(), offset.getZ()});
					NBTTagCompound targetNbt = new NBTTagCompound();
					target.writeToNBT(targetNbt);
					ItemNBTHelper.setTagCompound(stack, "targettingInfo", targetNbt);
				}
				else
				{
					int[] array = ItemNBTHelper.getIntArray(stack, "linkingPos");
					BlockPos linkPos = new BlockPos(array[1],array[2],array[3]);
					Vec3i offsetLink = BlockPos.NULL_VECTOR;
					if (array.length==7)
						offsetLink = new Vec3i(array[4], array[5], array[6]);
					TileEntity tileEntityLinkingPos = world.getTileEntity(linkPos);
					int distanceSq = (int) Math.ceil( linkPos.distanceSq(masterPos) );
					if(array[0]!=world.provider.getDimension())
						player.sendMessage(new TextComponentTranslation(Lib.CHAT_WARN+"wrongDimension"));
					else if(linkPos.equals(masterPos))
						player.sendMessage(new TextComponentTranslation(Lib.CHAT_WARN+"sameConnection"));
					else if( distanceSq > (wire.getMaxLength()*wire.getMaxLength()))
						player.sendMessage(new TextComponentTranslation(Lib.CHAT_WARN+"tooFar"));
					else
					{
						TargetingInfo targetLink = TargetingInfo.readFromNBT(ItemNBTHelper.getTagCompound(stack, "targettingInfo"));
						//If there was some sort of call out here in the original function, we might not need to copy this function.
						if(!(tileEntityLinkingPos instanceof IImmersiveConnectable)||!((IImmersiveConnectable) tileEntityLinkingPos).canConnectCable(wire, targetLink, offset) || !canConnectCable(wire, tileEntityLinkingPos))
							player.sendMessage(new TextComponentTranslation(Lib.CHAT_WARN+"invalidPoint"));
						else
						{
							IImmersiveConnectable nodeHere = (IImmersiveConnectable)tileEntity;
							IImmersiveConnectable nodeLink = (IImmersiveConnectable)tileEntityLinkingPos;
							boolean connectionExists = false;
							Set<Connection> outputs = ImmersiveNetHandler.INSTANCE.getConnections(world, Utils.toCC(nodeHere));
							if(outputs!=null)
								for(Connection con : outputs)
								{
									if(con.end.equals(Utils.toCC(nodeLink)))
										connectionExists = true;
								}
							if(connectionExists)
								player.sendMessage(new TextComponentTranslation(Lib.CHAT_WARN+"connectionExists"));
							else
							{
								Set<BlockPos> ignore = new HashSet<>();
								ignore.addAll(nodeHere.getIgnored(nodeLink));
								ignore.addAll(nodeLink.getIgnored(nodeHere));
								Connection tmpConn = new Connection(Utils.toCC(nodeHere), Utils.toCC(nodeLink), wire,
										(int)Math.sqrt(distanceSq));
								Vec3d start = nodeHere.getConnectionOffset(tmpConn, target, pos.subtract(masterPos)).addVector(tileEntity.getPos().getX(),
										tileEntity.getPos().getY(), tileEntity.getPos().getZ());
								Vec3d end = nodeLink.getConnectionOffset(tmpConn, targetLink, offsetLink).addVector(tileEntityLinkingPos.getPos().getX(),
										tileEntityLinkingPos.getPos().getY(), tileEntityLinkingPos.getPos().getZ());
								boolean canSee = ApiUtils.raytraceAlongCatenary(tmpConn, (p)->{
									if (ignore.contains(p.getLeft()))
										return false;
									IBlockState state = world.getBlockState(p.getLeft());
									return ApiUtils.preventsConnection(world, p.getLeft(), state, p.getMiddle(), p.getRight());
								}, (p)->{}, start, end);
								if(canSee)
								{
									Connection conn = ImmersiveNetHandler.INSTANCE.addAndGetConnection(world, Utils.toCC(nodeHere), Utils.toCC(nodeLink),
											(int)Math.sqrt(distanceSq), wire);


									nodeHere.connectCable(wire, target, nodeLink, offset);
									nodeLink.connectCable(wire, targetLink, nodeHere, offsetLink);
									ImmersiveNetHandler.INSTANCE.addBlockData(world, conn);
									IESaveData.setDirty(world.provider.getDimension());
									Utils.unlockIEAdvancement(player, "main/connect_wire");

									if(!player.capabilities.isCreativeMode)
										stack.shrink(1);
									((TileEntity)nodeHere).markDirty();
									world.addBlockEvent(masterPos, ((TileEntity) nodeHere).getBlockType(), -1, 0);
									IBlockState state = world.getBlockState(masterPos);
									world.notifyBlockUpdate(masterPos, state,state, 3);
									((TileEntity)nodeLink).markDirty();
									world.addBlockEvent(linkPos, ((TileEntity) nodeLink).getBlockType(), -1, 0);
									state = world.getBlockState(linkPos);
									world.notifyBlockUpdate(linkPos, state,state, 3);
								}
								else
									player.sendMessage(new TextComponentTranslation(Lib.CHAT_WARN+"cantSee"));
							}
						}
					}
					ItemNBTHelper.remove(stack, "linkingPos");
					ItemNBTHelper.remove(stack, "targettingInfo");
				}
			return EnumActionResult.SUCCESS;
		}
		return EnumActionResult.PASS;
	}

	public boolean canConnectCable(WireType wire, TileEntity targetEntity) {
		//We specifically only support whitelisted TEs here.
		//Without this, you can connect the AF wire to any connectable block that doesn't specifically deny it.
		if (!(targetEntity instanceof TileEntityRelayAF) &&
			!(targetEntity instanceof TileEntityTransformerAF) &&
			!(targetEntity instanceof TileEntityRedstoneBreaker) &&
			!(targetEntity instanceof TileEntityEnergyMeter) &&
			!(targetEntity instanceof TileEntityFeedthrough))
			return false;
		return true;
	}*/
	
	/*@Nonnull
	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
	{
		return ApiUtils.doCoilUse(this, player, world, pos, hand, side, hitX, hitY, hitZ);
	}

}*/
