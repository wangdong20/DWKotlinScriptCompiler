/**
 * Efficient way to find and print all prime numbers less or equal to given number n
 */
 fun findAndPrintPrimes(n : Int) {
    var primes = Array(n + 1, {i -> true});     // Semicolon here, semicolon is not required

    for(k in 2..n / k + 1) {
        if(primes[k]) {
            for(i in k..n / k + 1) {
                primes[k * i] = false   // k * i is not prime
            }
        }
    }

    val NUMPERLINE = 10    // Read-only variable cannot be modified
    var count = 0               // Count the number of prime numbers so far

    // Print prime numbers
    if(n < 3) {
        println(2)
        count++
    } else {
         print("2     ")
         count++
         for(i in 3..n + 1 step 2) {
            if(primes[i]) {
                 count++;
                 if(count % NUMPERLINE == 0) {
                          println("$i     ")
                 } else {
                          print("$i     ")
                 }
            }
         }
     }

    println()
    println()
    println("$count prime(s) less than or equal to $n")       // String interpolation here
 }

 findAndPrintPrimes(1000)