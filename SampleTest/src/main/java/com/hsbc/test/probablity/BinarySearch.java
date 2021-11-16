package com.hsbc.test.probablity;

public class BinarySearch {


    public static int indexOfClosestElement(Float searchArray[], float target)
    {
        int length = searchArray.length;
        if (target <= searchArray[0]) {
            return 0;
        }
        if (target >= searchArray[length - 1]) {
            return length - 1;
        }

        int i = 0, j = length, mid = 0;
        while (i < j) {
            mid = (i + j) / 2;

            if (searchArray[mid] == target) {
                return mid;
            }

            if (target < searchArray[mid]) {
                if (mid > 0 && target > searchArray[mid - 1]) {
                    return findClosestIndex(searchArray, mid - 1, mid, target);
                }
                j = mid;
            } else {
                if (mid < length-1 && target < searchArray[mid + 1]) {
                    return findClosestIndex(searchArray, mid, mid + 1, target);
                }
                i = mid + 1;
            }
        }
        return mid;
    }

    public static int findClosestIndex(Float searchArray[], int index1, int index2, float target)
    {
        if (target - searchArray[index1] >= searchArray[index2] - target) {
            return index2;
        } else {
            return index1;
        }
    }
}
