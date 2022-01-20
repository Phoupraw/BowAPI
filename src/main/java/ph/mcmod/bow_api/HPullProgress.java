package ph.mcmod.bow_api;

import net.minecraft.entity.projectile.PersistentProjectileEntity;

/**
 * 有拉弓进度属性<br>
 * {@link PersistentProjectileEntity}实现了此接口。
 */
public interface HPullProgress {
	void setPullProgress(double value);
	double getPullProgress();
}
