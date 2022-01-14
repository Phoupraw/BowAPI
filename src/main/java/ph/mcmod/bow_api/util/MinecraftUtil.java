package ph.mcmod.bow_api.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonSyntaxException;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.Tag;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * @version 0.4.0
 */
@SuppressWarnings("unused")
public final class MinecraftUtil {

/**
 * 按照小箱子的GUI排布来获取格子的X坐标
 *
 * @param m 格子所在的列数-1
 * @return 格子的X坐标
 */
public static int x(int m) {
	return 8 + m * 18;
}

/**
 * 按照小箱子的GUI排布来获取格子的Y坐标
 *
 * @param n 格子所在的行数-1
 * @return 格子的Y坐标
 */
public static int y(int n) {
	return 18 + n * 18;
}

/**
 * 将tick值转化为计时时间
 *
 * @return **:**:**
 */
public static String ticksToTime(int ticks) {
	int seconds = (int) Math.ceil((double) ticks / 20D);
	int hours = seconds / 60 / 60;
	int minutes = (seconds - (hours * 60 * 60)) / 60;
	seconds = seconds - (hours * 60 * 60) - (minutes * 60);
	return String.format("%s:%s:%s", addZero(hours), addZero(minutes), addZero(seconds));
}

private static String addZero(int num) {
	return num < 10 ? "0" + num : String.valueOf(num);
}

/**
 * 给物品添加文本lore
 *
 * @param item             要添加lore的物品
 * @param isText           是否是text
 * @param textOrTranslated 名字
 * @param color            颜色
 * @param italic           倾斜
 * @param bold             粗体
 * @param underlined       下划线
 * @param strikethrough    删除线
 * @param obfuscated       乱码
 */
public static void addLore(ItemStack item, boolean isText, String textOrTranslated, @Nullable String color, boolean italic, boolean bold, boolean underlined, boolean strikethrough, boolean obfuscated) {
	String text = "{";
	if (isText)
		text += addLoreString("text", textOrTranslated);
	else
		text += addLoreString("translate", textOrTranslated);
	if (color != null)
		text += addLoreString("color", color);
	if (!italic)
		text += addLoreString("italic", false);
	if (bold)
		text += addLoreString("bold", true);
	if (underlined)
		text += addLoreString("underlined", true);
	if (strikethrough)
		text += addLoreString("strikethrough", true);
	if (obfuscated)
		text += addLoreString("obfuscated", true);
	text = text.substring(0, text.length() - 1) + "}";
	addLore(item, NbtString.of(text));
}

private static String addLoreString(String key, Object value) {
	return String.format("\"%s\":\"%s\",", key, value);
}

/**
 * 给物品添加lore
 *
 * @param item 要添加lore的物品
 * @param lore 要添加的lore
 */
public static void addLore(ItemStack item, NbtString lore) {
	NbtCompound nbt = item.getOrCreateNbt();
	if (nbt.contains("display")) {
		if (nbt.contains("Lore")) {
			nbt.getCompound("display").getList("Lore", 8).add(lore);
		} else {
			NbtList list = new NbtList();
			list.add(lore);
			nbt.getCompound("display").put("Lore", list);
		}
	} else {
		NbtCompound display = new NbtCompound();
		NbtList list = new NbtList();
		list.add(lore);
		display.put("Lore", list);
		nbt.put("display", display);
	}
}

/**
 * 获取玩家指向的物品实体
 *
 * @param player 玩家
 * @return 物品实体
 * @author Cjsah
 */
@Nullable
public static ItemEntity rayItem(PlayerEntity player) {
	double length = .05D;
	Vec3d playerPos = player.getCameraPosVec(1.0F);
	double yaw = player.getYaw();
	double pitch = player.getPitch();
	double y = -Math.sin(pitch * Math.PI / 180D) * length;
	double x = -Math.sin(yaw * Math.PI / 180D);
	double z = Math.cos(yaw * Math.PI / 180D);
	double proportion = Math.sqrt((((length * length) - (y * y)) / ((x * x) + (z * z))));
	x *= proportion;
	z *= proportion;
	for (Vec3d pos = playerPos; Math.sqrt(Math.pow(pos.x - playerPos.x, 2) + Math.pow(pos.y - playerPos.y, 2) + Math.pow(pos.z - playerPos.z, 2)) < 5; pos = pos.add(x, y, z)) {
		if (player.world.getBlockState(new BlockPos(pos)).getBlock() != Blocks.AIR) {
			return null;
		}
		Box box = new Box(pos.x - 0.005, pos.y - 0.2, pos.z - 0.005, pos.x + 0.005, pos.y + 0.005, pos.z + 0.005);
		List<ItemEntity> list = player.world.getEntitiesByClass(ItemEntity.class, box, (entity) -> entity != null && entity.isAlive());
		if (!list.isEmpty()) {
			return list.get(0);
		}
	}
	return null;
}

/**
 * 使某坐标向玩家的前方移动一段距离
 *
 * @param pos    位移前坐标
 * @param length 位移长度
 * @return 位移后的坐标
 * @author Cjsah
 */
public static Vec3d move(PlayerEntity player, Vec3d pos, float length) {
	double yaw = player.getYaw();
	double x = -Math.sin(yaw * Math.PI / 180D) * length;
	double z = Math.cos(yaw * Math.PI / 180D) * length;
	return pos.add(x, 0, z);
}

/**
 * 吸引物品
 *
 * @param pos         物品需要被吸引到的地方
 * @param serverWorld 需要吸引物品的世界
 * @param needPickup  拾取延迟需要为0
 * @param tp          直接传送
 */
public static void attractItems(Vec3d pos, ServerWorld serverWorld, boolean needPickup, boolean tp) {
	for (ItemEntity itemEntity : serverWorld.getEntitiesByType(EntityType.ITEM, new Box(pos, pos).expand(16), itemEntity -> pos.isInRange(itemEntity.getPos(), 16) && (!needPickup || !itemEntity.cannotPickup()))) {
		if (tp) {
			itemEntity.teleport(pos.x, pos.y, pos.z);
			for (ServerPlayerEntity serverPlayerEntity : serverWorld.getPlayers(player -> pos.distanceTo(player.getPos()) < 32)) {
				serverPlayerEntity.networkHandler.sendPacket(new EntityPositionS2CPacket(itemEntity));
			}
		} else {
			Vec3d v = pos.subtract(itemEntity.getPos());
			double d = pos.distanceTo(itemEntity.getPos());
			if (d > 1)
				v = v.multiply(1 / v.length());
			itemEntity.setVelocity(v);
			for (ServerPlayerEntity serverPlayerEntity : serverWorld.getPlayers(player -> pos.distanceTo(player.getPos()) < 32)) {
				serverPlayerEntity.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(itemEntity));
			}
		}
	}
}

/**
 * 挖掘方块
 *
 * @param world    世界
 * @param entity   玩家
 * @param pos      位置
 * @param droppeds 掉落物列表，方块的掉落物将会被加入该列表，如果为{@code null}，则掉落物直接消失
 */
public static void excavate(ServerWorld world, LivingEntity entity, BlockPos pos, @Nullable List<ItemStack> droppeds) {
	BlockState blockState = world.getBlockState(pos);
	Block block = blockState.getBlock();
	BlockEntity blockEntity = world.getBlockEntity(pos);
	ItemStack toolStack = entity.getMainHandStack();
	if (canHarvest(toolStack, blockState, world, pos)) {
		boolean b = true;
		if (entity instanceof ServerPlayerEntity player) {
			b = !player.isCreative();
			if (b) {
				player.incrementStat(Stats.MINED.getOrCreateStat(block));
				toolStack.postMine(world, blockState, pos, player);
			}
		} else {
			if (blockState.getHardness(world, pos) != 0)
				toolStack.damage(1, world.random, null);
		}
		if (b) {
			Block.dropStacks(blockState, world, pos, null, entity, toolStack);
			if (droppeds != null) {
				droppeds.addAll(Block.getDroppedStacks(blockState, world, pos, blockEntity, entity, toolStack));
			}
		}
		world.breakBlock(pos, false, entity);
	}
}

/**
 * 测试工具能否挖掘方块，考虑基岩之类的把硬度设为{@code -1}的方块
 *
 * @param stack 工具
 * @param state 方块
 * @param world 世界
 * @param pos   位置
 */
public static boolean canHarvest(ItemStack stack, BlockState state, World world, BlockPos pos) {
//		System.out.println(state.getBlock() + ": " + state.getHardness(world, pos));
	return canHarvest(stack, state) && state.getHardness(world, pos) >= 0;
}

/**
 * 测试工具能否挖掘方块，但不考虑基岩之类的把硬度设为{@code -1}的方块
 *
 * @param stack 工具
 * @param state 方块
 */
public static boolean canHarvest(ItemStack stack, BlockState state) {
	return !state.isToolRequired() || stack.isSuitableFor(state);
}

/**
 * 给予物品
 *
 * @param player 玩家
 * @param stacks 物品
 */
public static void give(PlayerEntity player, ItemStack... stacks) {
	for (ItemStack stack : stacks) {
		int maxCount = stack.getMaxCount();
		int count = stack.getCount();
		while (count > 0) {
			int actual = Math.min(maxCount, count);
			stack.setCount(actual);
			count -= actual;
			ItemEntity itemEntity = new ItemEntity(player.world, player.getX(), player.getY(), player.getZ(), stack);
			itemEntity.setOwner(player.getUuid());
			player.world.spawnEntity(itemEntity);
		}
	}
}

/**
 * 掉落物品
 *
 * @param world  世界
 * @param pos    位置
 * @param stacks 被掉落的物品
 */
public static ItemEntity[] drop(World world, Vec3d pos, ItemStack... stacks) {
	return drop(world, pos, Arrays.asList(stacks));
}

/**
 * 掉落物品
 *
 * @param world  世界
 * @param pos    位置
 * @param stacks 被掉落的物品
 */
public static ItemEntity[] drop(World world, Vec3d pos, List<ItemStack> stacks) {
	ItemEntity[] itemEntities = new ItemEntity[stacks.size()];
	for (int i = 0; i < stacks.size(); i++) {
		ItemEntity itemEntity = new ItemEntity(world, pos.x, pos.y, pos.z, stacks.get(i));
		itemEntity.setToDefaultPickupDelay();
		world.spawnEntity(itemEntity);
		itemEntities[i] = itemEntity;
	}
	return itemEntities;
}

/**
 * 在物品栏间移动物品
 *
 * @param source 源物品栏
 * @param target 目标物品栏
 */
public static void transfer(Inventory source, Inventory target) {
	for (int i = 0; i < source.size(); i++) {
		ItemStack stack1 = source.getStack(i);
		for (int j = 0; j < target.size() && !stack1.isEmpty(); j++) {
			ItemStack stack2 = target.getStack(j);
			if (stack2.isEmpty()) {
				target.setStack(j, stack1.copy());
				stack1.decrement(stack1.getCount());
			} else if (ItemStack.areItemsEqual(stack1, stack2) && ItemStack.areNbtEqual(stack1, stack2)) {
				int c = stack1.getMaxCount() - stack1.getCount() - stack2.getCount();
				stack2.increment(c);
				stack1.decrement(c);
			}
		}
	}
}

/**
 * 把{@link Inventory}储存到{@link NbtCompound}
 *
 * @param inventory 物品栏
 * @param nbt       标签
 */
public static void inventoryToNbt(Inventory inventory, NbtCompound nbt) {
	DefaultedList<ItemStack> stacks = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);
	for (int i = 0; i < stacks.size(); i++) {
		stacks.set(i, inventory.getStack(i));
	}
	Inventories.writeNbt(nbt, stacks);
}

/**
 * 从{@link NbtCompound}读取{@link Inventory}
 *
 * @param inventory 物品栏
 * @param nbt       标签
 */
public static void inventoryFromNbt(Inventory inventory, NbtCompound nbt) {
	DefaultedList<ItemStack> stacks = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);
	Inventories.readNbt(nbt, stacks);
	for (int i = 0; i < inventory.size(); i++) {
		inventory.setStack(i, stacks.get(i));
	}
}

/**
 * 用经验值修复物品，就像经验修补
 *
 * @param stack 损坏的物品
 * @param exp   经验值
 * @return 剩余的经验值
 */
public static int mend(ItemStack stack, int exp) {
	if (stack.isDamaged()) {
		int amount = Math.min(stack.getDamage(), exp * 2);
		stack.setDamage(stack.getDamage() - amount);
		exp -= amount / 2 + ((amount & 2) == 0 ? 0 : (Math.random() < .5 ? 1 : 0));
	}
	return exp;
}

/**
 * 拾取经验球并转化为经验值
 *
 * @param world  世界
 * @param pos    位置
 * @param radius 半径或半棱长
 * @param globe  是球体
 * @return 拾取到的经验值
 */
public static int collectExpOrbs(World world, Vec3d pos, double radius, boolean globe) {
	int exp = 0;
	for (ExperienceOrbEntity orb : world.getEntitiesByClass(ExperienceOrbEntity.class, new Box(pos, pos).expand(radius), orb -> !globe || orb.getPos().isInRange(pos, radius))) {
		exp += orb.getExperienceAmount();
		orb.discard();
	}
	return exp;
}

/**
 * 如果当前状态效果的倍率等于指定的倍率，时间小于等于指定的时间，如果有隐藏的状态效果，则使用隐藏的状态效果，否则直接移除
 *
 * @param living    生物
 * @param effect    状态效果
 * @param amplifier 倍率
 * @param duration  时间
 */
public static void removeEffectExceptHidden(LivingEntity living, StatusEffect effect, int amplifier, int duration) {
	StatusEffectInstance effectInstance = living.getStatusEffect(effect);
	if (effectInstance != null && effectInstance.getAmplifier() == amplifier && effectInstance.getDuration() <= duration) {
		NbtCompound nbt1 = effectInstance.writeNbt(new NbtCompound());
		if (nbt1.contains("HiddenEffect")) {
			NbtCompound nbt2 = nbt1.getCompound("HiddenEffect");
			living.addStatusEffect(StatusEffectInstance.fromNbt(nbt2));
		} else {
			living.removeStatusEffect(effect);
		}
	}
}

public static void actionbar(ServerPlayerEntity player, String translationKey, Object... params) {
	player.sendMessage(new TranslatableText(translationKey, params), true);
}

public static void tellraw(ServerPlayerEntity player, String translationKey, Object... params) {
	player.sendMessage(new TranslatableText(translationKey, params), false);
}

public static <T> ImmutableList<T> findByKeyword(DefaultedRegistry<T> registry, String keyword) {
	ImmutableList.Builder<T> builder = ImmutableList.builder();
	for (Map.Entry<RegistryKey<T>, T> entry : registry.getEntries()) {
		if (entry.getKey().getValue().getPath().contains(keyword))
			builder.add(entry.getValue());
	}
	return builder.build();
}

public static boolean canInsert(List<ItemStack> items, Inventory inventory, int beginIndex, int endIndex) {
	List<ItemStack> copy = Lists.newArrayListWithCapacity(endIndex - beginIndex);
	for (int i = beginIndex; i < endIndex; i++) {
		copy.add(inventory.getStack(i).copy());
	}
	for (ItemStack stack : items) {
		for (int i = 0; i < copy.size() && !stack.isEmpty(); i++) {
			if (copy.get(i).isEmpty()) {
				copy.set(i, stack.copy());
				stack.decrement(stack.getCount());
			} else {
				ItemStack stack2 = copy.get(i);
				if (equal(stack, stack2)) {
					int c = Math.min(stack.getCount(), stack2.getMaxCount() - stack2.getCount());
					stack.decrement(c);
					stack2.increment(c);
				}
			}
		}
		if (!stack.isEmpty()) {
			return false;
		}
	}
	return true;
}

public static boolean equal(ItemStack stack1, ItemStack stack2) {
	return ItemStack.areItemsEqual(stack1, stack2) && ItemStack.areNbtEqual(stack1, stack2);
}

public static void insert(List<ItemStack> items, Inventory inventory, int beginIndex, int endIndex) {
	for (ItemStack stack : items) {
		for (int i = beginIndex; i < endIndex && !stack.isEmpty(); i++) {
			if (inventory.getStack(i).isEmpty()) {
				inventory.setStack(i, stack.copy());
				stack.decrement(stack.getCount());
			} else {
				ItemStack stack2 = inventory.getStack(i);
				if (equal(stack, stack2)) {
					int c = Math.min(stack.getCount(), stack2.getMaxCount() - stack2.getCount());
					stack.decrement(c);
					stack2.increment(c);
				}
			}
		}
	}
}

public static <T> Tag<T> getTag(String id, RegistryKey<Registry<T>> registryKey) {
	return ServerTagManagerHolder.getTagManager().getTag(registryKey, new Identifier(id), id1 -> new JsonSyntaxException("Unknown item tag '" + id1 + "'"));
}

public static void serverPlayerSound(World world, PlaySoundS2CPacket packet) {
	if (world instanceof ServerWorld serverWorld) {
		Box box = new Box(packet.getX(), packet.getY(), packet.getZ(), packet.getX(), packet.getY(), packet.getZ()).expand(packet.getVolume() * 16);
		for (ServerPlayerEntity player : serverWorld.getPlayers(player -> box.contains(player.getPos()))) {
			player.networkHandler.sendPacket(packet);
		}
	}
}

public static void addPlayerSlots(Consumer<Slot> addSlot, PlayerInventory playerInventory) {
	addPlayerSlots(addSlot, playerInventory, 8, 84);
}

public static void addPlayerSlots(Consumer<Slot> addSlot, PlayerInventory playerInventory, int x, int y) {
	for (int m = 0; m < 3; ++m) {
		for (int l = 0; l < 9; ++l)
			addSlot.accept(new Slot(playerInventory, l + m * 9 + 9, x + l * 18, y + m * 18));
	}
	for (int m = 0; m < 9; ++m)
		addSlot.accept(new Slot(playerInventory, m, x + m * 18, y + 58));
}

public static OptionalInt openScreen(PlayerEntity user, BiFunction<Integer, PlayerInventory, ScreenHandler> constructor, Text title) {
	return user.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, playerInventory, player) -> constructor.apply(syncId, playerInventory), title));
}

/**
 * 获取物品ID并{@link Identifier#getPath()}
 */
public static String getPath(Item item) {
	return getId(item).getPath();
}

/**
 * 获取物品ID
 */
public static Identifier getId(Item item) {
	return Registry.ITEM.getId(item);
}

/**
 * 获取方块ID
 */
public static Identifier getId(Block block) {
	return Registry.BLOCK.getId(block);
}

/**
 * 设置{@link System#out}，使其输出方法栈，用于监听是何处调用了{@link System#out}
 */
public static void monitorSOut() {
	System.setOut(new PrintStream(System.out) {
		@Override
		public void println() {
			super.println();
			new Throwable().printStackTrace();
		}

		@Override
		public void println(boolean x) {
			super.println(x);
			new Throwable().printStackTrace();
		}

		@Override
		public void println(char x) {
			super.println(x);
			new Throwable().printStackTrace();
		}

		@Override
		public void println(int x) {
			super.println(x);
			new Throwable().printStackTrace();
		}

		@Override
		public void println(long x) {
			super.println(x);
			new Throwable().printStackTrace();
		}

		@Override
		public void println(float x) {
			super.println(x);
			new Throwable().printStackTrace();
		}

		@Override
		public void println(double x) {
			super.println(x);
			new Throwable().printStackTrace();
		}

		@Override
		public void println(char @NotNull [] x) {
			super.println(x);
			new Throwable().printStackTrace();
		}

		@Override
		public void println(@Nullable String x) {
			super.println(x);
			new Throwable().printStackTrace();
		}

		@Override
		public void println(@Nullable Object x) {
			super.println(x);
			new Throwable().printStackTrace();
		}
	});
}

/**
 * 把实体包装成玩家，用于调用使用物品、与实体互动等方法。<br>
 *
 * @param entity 被包装的实体
 * @return 包装成的玩家。如果{@code entity}是客户端的，则返回{@link net.minecraft.client.network.ClientPlayerEntity}；否则返回{@link ServerPlayerEntity}
 */
public static PlayerEntity delegate(Entity entity) {
	var player = delegate(entity.world, entity.getUuid(), entity.getName().asString());
	player.setPosition(entity.getPos());
	player.setVelocity(entity.getVelocity());
	player.setYaw(entity.getYaw());
	player.setPitch(entity.getPitch());
	if (entity instanceof LivingEntity living) {
		for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
			player.equipStack(equipmentSlot, living.getEquippedStack(equipmentSlot));
		}
	}
	return player;
}

/**
 * 构造一个玩家
 *
 * @param world 世界
 * @param uuid  UUID
 * @param name  玩家名
 * @return 玩家。如果{@code world}是客户端的，则返回{@link net.minecraft.client.network.ClientPlayerEntity}；否则返回{@link ServerPlayerEntity}
 * @see #delegate(Entity)
 */
public static PlayerEntity delegate(World world, UUID uuid, String name) {
	PlayerEntity player;
	var gameProfile = new GameProfile(uuid, name);
	if (world.isClient()) {
		player = new OtherClientPlayerEntity((ClientWorld) world, gameProfile);
//		((ClientPlayerEntity)player).networkHandler = MinecraftClient.getInstance().getNetworkHandler();
	} else {
		var serverWorld = (ServerWorld) world;
		ServerPlayerEntity serverPlayer = new ServerPlayerEntity(serverWorld.getServer(), serverWorld, gameProfile);
		serverPlayer.networkHandler = new ServerPlayNetworkHandler(serverWorld.getServer(), new ClientConnection(NetworkSide.SERVERBOUND), serverPlayer);
		player = serverPlayer;
	}
	return player;
}

/**
 * 将{@code source}的各个属性转移到{@code target}上，并把它的加速度设置为速度的十分之一，然后在{@code source}的世界里<b>生成</b>{@code target}，随后销毁{@code source}。<br>
 * 已经生成了转换后的实体，不需要额外调用{@link World#spawnEntity(Entity)}
 *
 * @return {@code target}
 */
public static <T extends Entity> T transfer(ProjectileEntity source, T target) {
	target.setPosition(source.getPos());
	target.setVelocity(source.getVelocity());
	target.noClip = source.noClip;
	target.setInvulnerable(source.isInvulnerable());
	target.setInvisible(source.isInvisible());
	target.setCustomName(source.getCustomName());
	target.setFireTicks(source.getFireTicks());
	target.setAir(source.getAir());
	target.setGlowing(source.isGlowing());
	target.setCustomNameVisible(source.isCustomNameVisible());
	target.setFrozenTicks(source.getFrozenTicks());
	target.setSilent(source.isSilent());
	target.setPose(source.getPose());
	if (target instanceof ProjectileEntity projectile) {
		projectile.setOwner(source.getOwner());
		if (projectile instanceof ExplosiveProjectileEntity explosive) {
			var power = source.getVelocity().multiply(0.1);
			explosive.powerX = power.x;
			explosive.powerY = power.y;
			explosive.powerZ = power.z;
		} else if (source instanceof PersistentProjectileEntity sp && projectile instanceof PersistentProjectileEntity p) {
			copy(sp, p);
		}
	}
	source.world.spawnEntity(target);
	source.discard();
	return target;
}

/**
 * 先把{@code source}设为无敌，然后执行{@code task}，最后把{@code source}的无敌设置回去。
 */
public static void invulnerable(Entity source, Runnable task) {
	if (source != null)
		temp(source.isInvulnerable(), source::setInvulnerable, true, task);
	else
		task.run();
}

/**
 * 临时修改某个值，完事之后再改回去。
 *
 * @param preValue 改之前的值
 * @param setter   用于把值改回去
 * @param expected 要改的值
 * @param task     在改了值之后要执行的代码
 */
public static <T> void temp(T preValue, Consumer<T> setter, T expected, Runnable task) {
	setter.accept(expected);
	task.run();
	setter.accept(preValue);
}

/**
 * 对着实体使用物品，如果失败就对着方块使用，如果失败就对着空气使用。
 *
 * @param player    使用物品的玩家
 * @param hitResult 提供实体、使用物品的位置
 * @return 使用结果
 */
public static ActionResult use(ServerPlayerEntity player, HitResult hitResult) {
	var result = ActionResult.PASS;
	var hand = Hand.MAIN_HAND;
	var itemStack = player.getStackInHand(hand);
	if (hitResult instanceof EntityHitResult entityHitResult) {
		Entity entity = entityHitResult.getEntity();
		result = entity.interactAt(player, hitResult.getPos(), hand);
		if (!result.isAccepted())
			result = entity.interact(player, hand);
	}
	if (!result.isAccepted()) {
		BlockHitResult blockHitResult = blockHitResult(hitResult, player.getMovementDirection().getOpposite());
		result = player.world.getBlockState(blockHitResult.getBlockPos()).onUse(player.world, player, hand, blockHitResult);
		if (!result.isAccepted()) {
			result = player.interactionManager.interactBlock(player, player.world, itemStack, hand, blockHitResult);
			if (!result.isAccepted())
				result = player.interactionManager.interactItem(player, player.world, itemStack, hand);
		}
	}
	return result;
}

/**
 * 将一个不一定是{@link BlockHitResult}的{@link HitResult}包装成{@link BlockHitResult}
 *
 * @param hitResult 被包装的
 * @param direction 用于构造{@link BlockHitResult}的构造函数参数，表示该事件发生在方块的哪个面
 * @return 包装后的
 */
public static BlockHitResult blockHitResult(HitResult hitResult, @Nullable Direction direction) {
	return hitResult instanceof BlockHitResult b ? b : new BlockHitResult(hitResult.getPos(), direction == null ? Direction.UP : direction, new BlockPos(hitResult.getPos()), false);
}

/**
 * 如果要在箭与方块或实体发生碰撞的位置放置方块，应该放置到什么位置
 *
 * @param hitResult {@link ProjectileEntity#onCollision(HitResult)}的参数
 * @param velocity  箭当前的运动速度
 * @return 方块要被放置的位置
 */
public static BlockPos placePos(HitResult hitResult, Vec3d velocity) {
	return hitResult instanceof EntityHitResult entityHitResult ? entityHitResult.getEntity().getBlockPos() : new BlockPos(hitResult.getPos().subtract(velocity.multiply(0.01)));
}

/**
 * 找一个弹射物最近的目标，用于追踪
 *
 * @param projectile 弹射物
 * @return 目标
 */
public static LivingEntity findTarget(ProjectileEntity projectile) {
	return projectile.world.getClosestEntity(LivingEntity.class, TargetPredicate.DEFAULT.setPredicate(entity -> entity.isAttackable() && entity != projectile.getOwner()), null, projectile.getX(), projectile.getY(), projectile.getZ(), new Box(projectile.getPos(), projectile.getPos()).expand(8));
}

/**
 * 将一个装着{@link Serializable}的{@code collection}中的每个对象都转换成字节数组，并写入一个{@link NbtList}
 *
 * @param collection 装着可序列化对象的容器
 * @param nbtList    被写入的NBT
 * @return 参数 {@code nbtList}
 */
public static NbtList write(Collection<? extends Serializable> collection, NbtList nbtList) {
	collection.stream().map(serializable -> {
		try {
			return new NbtByteArray(JavaUtil.serialize(serializable));
		} catch (IOException e) {
			e.printStackTrace();
			return new NbtByteArray(new byte[0]);
		}
	}).forEach(nbtList::add);
	return nbtList;
}

/**
 * 从一个装着字节数组的{@link NbtList}读取{@link Serializable}，并依次传给{@code adder}
 *
 * @param nbtList 装着字节数组的列表
 * @param adder   生成的{@link Serializable}会依次传给这个参数
 * @param <T>     {@link Serializable}的实际类型
 */
@SuppressWarnings("unchecked")
public static <T extends Serializable> void read(NbtList nbtList, Consumer<T> adder) {
	nbtList.stream().map(nbtElement -> {
		if (nbtElement instanceof NbtByteArray nbtByteArray) {
			try {
				return (T) JavaUtil.deserialize(nbtByteArray.getByteArray());
			} catch (IOException | ClassNotFoundException | ClassCastException e) {
				e.printStackTrace();
			}
		}
		return null;
	}).filter(Objects::nonNull).forEach(adder);
}

/**
 * 召唤一个失重的、不掉落物品的{@link FallingBlockEntity}，模拟一个方块
 *
 * @param world      在这个世界召唤
 * @param pos        在这个位置召唤
 * @param blockState 要模拟的方块
 * @param duration   掉落的方块在多少游戏刻后消失
 * @return 已经调用了 {@link World#spawnEntity(Entity)}的假方块
 */
public static FallingBlockEntity simBlock(World world, Vec3d pos, BlockState blockState, int duration) {
	var fallingBlock = new FallingBlockEntity(world, pos.x, pos.y, pos.z, blockState);
	fallingBlock.timeFalling = 600 - duration;
	fallingBlock.dropItem = false;
	fallingBlock.setNoGravity(true);
	world.spawnEntity(fallingBlock);
	return fallingBlock;
}

/**
 * 搜索附近玩家
 *
 * @param center 搜索中心
 * @param radius 搜索半径
 * @return 玩家列表
 * @throws ClassCastException {@code center}不在服务端
 */
public static List<ServerPlayerEntity> nearbyPlayers(Entity center, double radius) throws ClassCastException {
	return nearbyPlayers((ServerWorld) center.world, center.getPos(), radius);
}

/**
 * 搜索附近玩家
 *
 * @param world  所在世界
 * @param center 搜索中心
 * @param radius 搜索半径
 * @return 玩家列表
 */
public static List<ServerPlayerEntity> nearbyPlayers(ServerWorld world, Vec3d center, double radius) {
	return world.getPlayers(player -> center.squaredDistanceTo(player.getPos()) <= radius * radius);
}

/**
 * 以指定向量的方向，根据右手螺旋定则，旋转一个三维向量指定角度。
 *
 * @param toBeRotated 被旋转的向量。旋转之后，这个向量不会改变。
 * @param shaft       转轴
 * @param degree      角度
 * @return 旋转后的向量
 */
public static Vec3d rotate(Vec3d toBeRotated, Vec3d shaft, double degree) {
	Quaternion rotation = new Quaternion(new Vec3f(shaft), (float) degree, true);
	Vec3f r0 = new Vec3f(toBeRotated);
	r0.rotate(rotation);
	return new Vec3d(r0);
}

/**
 * 烧炼物品
 *
 * @param world      配方所在的世界
 * @param ingredient 要烧炼的物品
 * @return 烧炼后的物品：如果{@code ingredient}是空或者没有对应的配方，则返回{@link ItemStack#EMPTY}；否则返回烧炼后的物品。
 */
public static ItemStack smell(ServerWorld world, ItemStack ingredient) {
	if (ingredient.isEmpty())
		return ItemStack.EMPTY;
	var recipeManager = world.getServer().getRecipeManager();
	var inventory = new SimpleInventory(ingredient);
	var recipe = recipeManager.getFirstMatch(RecipeType.SMELTING, inventory, world);
	if (recipe.isEmpty())
		return ItemStack.EMPTY;
	return recipe.get().craft(inventory);
}

/**
 * 从起点{@code start}按指定步长{@code step}迭代到终点{@code end}，每次迭代都会将迭代到的位置传给{@code consumer}，类似于这样：<br>
 * {@code for (Vec3d i = start; i < end; i += step)}<br>
 * &#9;{@code consumer.accept(i);}
 */
public static void stepInvoke(Vec3d start, Vec3d end, double step, Consumer<Vec3d> consumer) {
	double s = 0;
	Vec3d stepVec = end.subtract(start);
	double l = stepVec.length();
	stepVec = stepVec.multiply(step);
	Vec3d now = start;
	while (s < l) {
		s += step;
		consumer.accept(now);
		now = now.add(stepVec);
	}
}

/**
 * 复制一个{@link PersistentProjectileEntity}。
 */
@SuppressWarnings("unchecked")
public static <T extends PersistentProjectileEntity> T copy(T source) throws NullPointerException {
	T r = (T) source.getType().create(source.world);
	if (r == null)
		throw new NullPointerException("%s复制失败！".formatted(source));
	return copy(source, r);
}

public static < T extends PersistentProjectileEntity> T copy(PersistentProjectileEntity source, T target) {
	target.setPosition(source.getPos());
	target.setVelocity(source.getVelocity());
	target.setFireTicks(source.getFireTicks());
	target.setNoGravity(source.hasNoGravity());
	target.setOwner(source.getOwner());
	target.setDamage(source.getDamage());
	target.setPunch(source.getPunch());
	target.setPierceLevel(source.getPierceLevel());
	target.setCritical(source.isCritical());
	target.setShotFromCrossbow(source.isShotFromCrossbow());
	target.pickupType = source.pickupType;
	return target;
}

public static <T extends Entity> T copyNbt(Entity source, T target) {
	target.readNbt(source.writeNbt(new NbtCompound()));
	return target;
}

public static CreeperEntity setPowered(CreeperEntity creeper) {
	var nbt = creeper.writeNbt(new NbtCompound());
	nbt.putBoolean("powered",true);
	creeper.readNbt(nbt);
	return creeper;
}

private MinecraftUtil() {
}
}
