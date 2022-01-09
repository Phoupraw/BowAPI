package ph.mcmod.bow_api;

import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtList;

import java.io.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public interface Serialization extends Serializable {
static NbtByteArray serialize(Serializable lambda) {
	try (var bytesO = new ByteArrayOutputStream(); var objectO = new ObjectOutputStream(bytesO)) {
		objectO.writeObject(lambda);
		objectO.flush();
		return new NbtByteArray(bytesO.toByteArray());
	} catch (IOException e) {
		e.printStackTrace();
		throw new RuntimeException(e);
	}
}

@SuppressWarnings("unchecked")
static <T extends Serializable> T deserialize(NbtByteArray nbtBytes) {
	try {
		return (T) new ObjectInputStream(new ByteArrayInputStream(nbtBytes.getByteArray())).readObject();
	} catch (IOException | ClassNotFoundException e) {
		throw new RuntimeException(e);
	}
}

static NbtList serialize(List<? extends Serializable> list) {
	return list.stream().map(Serialization::serialize).collect(NbtList::new, NbtList::add, NbtList::add);
}

static <T extends Serializable> List<T> deserialize(NbtList nbtList) {
	return nbtList.stream().map(NbtByteArray.class::cast).<T>map(Serialization::deserialize).toList();
}

//@Override
//default void writeExternal(ObjectOutput out) {
//	if (getClass().getDeclaredFields().length > 0)
//		throw new RuntimeException("getClass().getDeclaredFields().length > 0");
//}
//
//@Override
//default void readExternal(ObjectInput in) {
//	if (getClass().getDeclaredFields().length > 0)
//		throw new RuntimeException("getClass().getDeclaredFields().length > 0");
//}

interface FFunction<T, R> extends Function<T, R>, Serialization {
}

interface FBiFunction<T, U, R> extends BiFunction<T, U, R>, Serialization {
}

interface FConsumer<T> extends Consumer<T>, Serialization {}

interface SBiConsumer<T, U> extends BiConsumer<T, U>, Serialization {

}
}
