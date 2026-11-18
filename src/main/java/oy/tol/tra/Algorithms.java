package oy.tol.tra;

import java.util.function.Predicate;
import java.util.Comparator;

/**
 * Generic algorithms for operating on arrays.
 */
public class Algorithms {

    /** Insertion sort for the supplied comparator demonstration. */
    public static <T> void sortWithComparator(T[] array, Comparator<T> comparator) {
        for (int index = 1; index < array.length; index++) {
            T value = array[index];
            int position = index;
            while (position > 0 && comparator.compare(array[position - 1], value) > 0) {
                array[position] = array[position - 1];
                position--;
            }
            array[position] = value;
        }
    }

    private static <T> void swap(T[] array, int first, int second) {
        T temporary = array[first];
        array[first] = array[second];
        array[second] = temporary;
    }

    /**
     * Reverses the elements of an array in place.
     *
     * @param <T> the element type
     * @param array the array to reverse
     */
    public static <T> void reverse(T[] array) {
       int i = 0;

       while (i < array.length / 2) {
           T temp = array[i];

           array[i] = array[array.length-i-1];
           array[array.length-i-1] = temp;

           i++;
        }
    }

    /**
     * Sorts the elements of an array in ascending order.
     *
     * @param <T> the element type
     * @param array the array to sort
     */
    public static <T extends Comparable<T>> void sort(T[] array){
      for (int i = 0; i < array.length - 1; i++) {
         int lowestIndex = i;
         for (int j = i + 1; j < array.length; j++) {
            if (array[j].compareTo(array[lowestIndex]) < 0) {
               lowestIndex = j;
            }
         }

         T temp = array[i];
         array[i] = array[lowestIndex];
         array[lowestIndex] = temp;
      }
    }

    /**
     * Searches a sorted array within the inclusive range, in O(log n) time.
     * Returns -1 when the value is absent or the range is empty.
     */
    public static <T extends Comparable<T>> int binarySearch(
            T aValue, T[] fromArray, int fromIndex, int toIndex) {
        while (fromIndex <= toIndex) {
            int middle = fromIndex + (toIndex - fromIndex) / 2;
            int comparison = fromArray[middle].compareTo(aValue);
            if (comparison == 0) {
                return middle;
            }
            if (comparison < 0) {
                fromIndex = middle + 1;
            } else {
                toIndex = middle - 1;
            }
        }
        return -1;
    }

    /** Sorts the array in place in ascending order using Quicksort. */
    public static <E extends Comparable<E>> void fastSort(E[] array) {
        quickSort(array, 0, array.length - 1);
    }

    /**
     * Sorts the inclusive range in place, with average O(n log n) time.
     * Recursing only on the smaller partition bounds stack space to O(log n).
     */
    public static <E extends Comparable<E>> void quickSort(E[] array, int begin, int end) {
        while (begin < end) {
            int boundary = partition(array, begin, end);
            if (boundary - begin < end - boundary) {
                quickSort(array, begin, boundary);
                begin = boundary + 1;
            } else {
                quickSort(array, boundary + 1, end);
                end = boundary;
            }
        }
    }

    /**
     * Hoare partitioning around the middle element. Equal values stop both
     * scans, so duplicate-heavy arrays still split into balanced partitions.
     * The returned index ends the left partition; it is not a pivot position.
     */
    private static <E extends Comparable<E>> int partition(E[] array, int begin, int end) {
        E pivot = array[begin + (end - begin) / 2];
        int left = begin;
        int right = end;
        while (true) {
            while (array[left].compareTo(pivot) < 0) {
                left++;
            }
            while (array[right].compareTo(pivot) > 0) {
                right--;
            }
            if (left >= right) {
                return right;
            }
            E temporary = array[left];
            array[left] = array[right];
            array[right] = temporary;
            left++;
            right--;
        }
    }
    public static <T> int partitionByRule(T[] array, int count, Predicate<T> rule) {
      // Find first element rules applies to.
      // Index of that element will be in variable index.
      int index = 0;
      for (; index < count; index++) {
         if (rule.test(array[index])) {
            break;
         }
      }
      // If went to the end, nothing was selected so quit here.
      if (index >= count) {
         return count;
      }
      // Then start finding not selected elements starting from next from index.
      // If the element is not selected, swap it with the selected one.
      int nextIndex = index + 1;
      // Until end of array reached.
      while (nextIndex != count) {
         if (!rule.test(array[nextIndex])) {
            swap(array, index, nextIndex);
            // If swapping was done, add to index since now it has non-selected element.
            index++;
         }
         nextIndex++;
      }
      return index;
   }

}
