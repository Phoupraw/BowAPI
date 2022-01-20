package ph.mcmod.bow_api;

import com.google.common.collect.ImmutableSet;
import net.fabricmc.fabric.impl.object.builder.FabricEntityType;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import ph.mcmod.bow_api.mixin.AccessEntityType;

import java.util.Objects;

/**
 * 更方便地创建实体类型。可以完全替代{@link FabricEntityType}与{@link EntityType}。<br>
 * 请用{@link #builder(EntityFactory)}来创建实例。
 */
public class SimpleEntityType<T extends Entity> extends FabricEntityType<T> {
public static class Builder<T extends Entity> {
	private final EntityFactory<T> factory;
	private SpawnGroup spawnGroup = SpawnGroup.MISC;
	private boolean savable = true;
	private boolean summonable = true;
	private boolean fireImmune = false;
	private boolean spawnableFarFromPlayer = false;
	private ImmutableSet<Block> spawnBlocks = ImmutableSet.of();
	private EntityDimensions entityDimensions = EntityDimensions.fixed(1, 1);
	private int maxTrackDistance = 4;
	private int trackTickInterval = 20;
	private Boolean alwaysUpdateVelocity = null;

	public Builder(EntityFactory<T> factory) {this.factory = factory;}

	public Builder<T> setSpawnGroup(SpawnGroup spawnGroup) {
		this.spawnGroup = spawnGroup;
		return this;
	}

	public Builder<T> setSavable(boolean savable) {
		this.savable = savable;
		return this;
	}

	public Builder<T> setSummonable(boolean summonable) {
		this.summonable = summonable;
		return this;
	}

	public Builder<T> setFireImmune(boolean fireImmune) {
		this.fireImmune = fireImmune;
		return this;
	}

	public Builder<T> setSpawnableFarFromPlayer(boolean spawnableFarFromPlayer) {
		this.spawnableFarFromPlayer = spawnableFarFromPlayer;
		return this;
	}

	public Builder<T> setSpawnBlocks(ImmutableSet<Block> spawnBlocks) {
		this.spawnBlocks = spawnBlocks;
		return this;
	}

	public Builder<T> setEntityDimensions(EntityDimensions entityDimensions) {
		this.entityDimensions = entityDimensions;
		return this;
	}

	public Builder<T> setEntityDimensions(float width, float height) {
		return setEntityDimensions(EntityDimensions.fixed(width, height));
	}

	public Builder<T> setMaxTrackDistance(int maxTrackDistance) {
		this.maxTrackDistance = maxTrackDistance;
		return this;
	}

	public Builder<T> setTrackTickInterval(int trackTickInterval) {
		this.trackTickInterval = trackTickInterval;
		return this;
	}

	public Builder<T> setAlwaysUpdateVelocity(Boolean alwaysUpdateVelocity) {
		this.alwaysUpdateVelocity = alwaysUpdateVelocity;
		return this;
	}

	public Builder<T> copyFrom(EntityType<?> entityType) {
		return this
		  .setSpawnGroup(entityType.getSpawnGroup())
		  .setSavable(entityType.isSaveable())
		  .setSummonable(entityType.isSummonable())
		  .setFireImmune(entityType.isFireImmune())
		  .setSpawnableFarFromPlayer(entityType.isSpawnableFarFromPlayer())
		  .setSpawnBlocks(((AccessEntityType) entityType).getCanSpawnInside())
		  .setEntityDimensions(entityType.getDimensions())
		  .setMaxTrackDistance(entityType.getMaxTrackDistance())
		  .setTrackTickInterval(entityType.getTrackTickInterval());
	}

	public Builder<T> copyFrom(FabricEntityType<?> entityType) {
		return copyFrom((EntityType<?>) entityType).setAlwaysUpdateVelocity(entityType.alwaysUpdateVelocity());
	}

	public SimpleEntityType<T> build() {
		return new SimpleEntityType<>(factory, spawnGroup, savable, summonable, fireImmune, spawnableFarFromPlayer, spawnBlocks, entityDimensions, maxTrackDistance, trackTickInterval, alwaysUpdateVelocity);
	}
}

public static <T extends Entity> Builder<T> builder(EntityFactory<T> factory) {
	return new Builder<>(factory);
}

protected SimpleEntityType(EntityFactory<T> factory, SpawnGroup spawnGroup, boolean savable, boolean summonable, boolean fireImmune, boolean spawnableFarFromPlayer, ImmutableSet<Block> spawnBlocks, EntityDimensions entityDimensions, int maxTrackDistance, int trackTickInterval, Boolean alwaysUpdateVelocity) {
	super(factory, spawnGroup, savable, summonable, fireImmune, spawnableFarFromPlayer, spawnBlocks, entityDimensions, maxTrackDistance, trackTickInterval, alwaysUpdateVelocity);
}
@NotNull
@Override
public T create(World world) {
	return Objects.requireNonNull(super.create(world), toString());
}
}
