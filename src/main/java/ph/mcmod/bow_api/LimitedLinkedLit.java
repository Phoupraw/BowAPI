package ph.mcmod.bow_api;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class LimitedLinkedLit<E> implements LimitedCollection<E> {
private final List<E> list = new LinkedList<>();

@Override
public void add(E e) {
	list.add(e);
}

@NotNull
@Override
public Iterator<E> iterator() {
	return LimitedCollection.immutable(list.iterator());
}
}
