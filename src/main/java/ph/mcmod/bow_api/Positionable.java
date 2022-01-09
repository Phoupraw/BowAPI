package ph.mcmod.bow_api;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface Positionable {
World getWorld();

Vec3d getPos();

}
