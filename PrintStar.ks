fun printStar(lines : Int) {
    // print top half
    for(i in 1..lines + 1) {
        for(j in 1..lines - i + 1) {
            print(" ")
        }
        for(k in 1..2 * i) {
            print("*")
        }
        println("")
    }

    // print bot half
    for(i in 1..lines) {
        for(j in 1..i + 1) {
            print(" ")
        }

        for(k in 1..-2 * i + 2 * lines) {
            print("*")
        }
        println("")
    }
}

printStar(15)