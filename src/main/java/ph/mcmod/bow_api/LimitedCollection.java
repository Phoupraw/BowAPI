package ph.mcmod.bow_api;

import java.util.Collection;
import java.util.Iterator;

public interface LimitedCollection<E> extends Iterable<E> {
/**
 * 把可修改的迭代器包装成不可修改的迭代器，调用返回的迭代器的{@link Iterator#remove()}会抛出{@link UnsupportedOperationException}
 *
 * @param iterator 要被包装的迭代器
 * @return 包装后的迭代器
 */
static <E> Iterator<E> immutable(Iterator<E> iterator) {
	return new Iterator<>() {
		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public E next() {
			return iterator.next();
		}
	};
}

/**
 * @see Collection#add(Object)
 */
void add(E e);

}
