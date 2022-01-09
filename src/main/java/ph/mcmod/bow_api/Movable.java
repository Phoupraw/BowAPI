package ph.mcmod.bow_api;

import net.minecraft.util.math.Vec3d;

public interface Movable extends Positionable {
void setPos(Vec3d value);

void setVelocity(Vec3d value);

Vec3d getVelocity();
}
