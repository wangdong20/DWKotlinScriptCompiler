fun bubbleSort(arr : Array<Int>, length: Int): Unit {
    for (i in 0..length - 1) {
        for (j in 0..length - i - 1) {
            if (arr[j] > arr[j + 1]) {
                // swap arr[j+1] and arr[i]
                var temp = arr[j];
                arr[j] = arr[j + 1];
                arr[j + 1] = temp;
            }
        }
    }
}

// Test for commend
var a = arrayOf(3, 2, 5, 6, 8, 9, 2, 4) // Create array with arrayOf expression
bubbleSort(a, 8)    // Function bubbleSort instance here
for(i in a) {
    println(i)
}