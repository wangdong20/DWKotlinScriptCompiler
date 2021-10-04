# DWKotlinScriptCompiler
![Java CI with Maven](https://github.com/wangdong20/DWKotlinScriptCompiler/workflows/Java%20CI%20with%20Maven/badge.svg)

This is Kotlin like language compiler that I try to write in Comp430 class.

### README.md
* en [English](README.md)
* zh_CN [简体中文](README_zh.md)

### Language name
DWKotlinScript

### Compiler Implementation Language and Reasoning
Java. I am familiar with Java and Java is compatible with Kotlin which is good to write my own version Kotlin like language by Java.

### Target Language
JVM bytecode.

### Language Description
Kotlin is a cross-platform, statically typed, general-purpose programming language with type inference. Kotlin is designed to interoperate fully with Java, and the JVM version of its standard library depends on the Java Class Library, but type inference allows its syntax to be more concise. In 2019 Google made Kotlin as the first language in Android development, I can see the benefit of using Kotlin language from Google’s announcement.

### Planned Restrictions
Kotlin is a power object oriented language. But I will make some limitation on my own Kotlin like language due to the lack of time and energy and the limitation of my own knowledge on Kotlin compiler. There will be no object in KotlinScript, in other word, no class in KotlinScript. Only support basic number type such as Int currently. I will also support String type, because String type is important in Kotlin. Array is also supported but only with basic generic type. It will support high order function, type inference. No garbage collection. Function declaration will not allow declare in block.

### Syntax
* var is variable
* fn is function name
* e is expression
* s is statement
* op is operator
* T is type variable
* P is Program
```
Basic type::= Int | String | Boolean | Unit | Any

type :: = Int | Boolean| String | Array<basic type> | ‘(‘type*’)’ -> type | T

e :: = ‘(‘ e ’)’ | e1 binop e2 | unop e |  fn’(‘e’)’ | var | arrayOf(var*) | Array(e1(Int), {e2((Int) -> basic type)}) | var = e | var += e | var -= e | var *= e | var /= e |‘{‘ (var : type)* -> e(return basic type) ‘}’ | '$'var | '$''{'e'}'

binop :: = ‘-’ | ‘+’ | ‘/’ | ‘*’ | ‘%’ | ‘||’ | ‘&&’ | ‘<’ | ‘>’ | ‘<=’ | ‘>=’ | ‘==’ | ‘!=’ | ‘+=’ | ‘-=’ | '*=' | '/=' | '='

unop :: = ‘!’ | ‘++’ | ‘--’

s :: = ‘if’ ‘(’ e ‘)’ ‘{‘ s* ‘}’ ‘else’ ‘{‘ s* ‘}’ | ‘if’ ‘(’e’)’ ’{’ s* ‘}’ | ‘while’ ‘(’ e ‘)’ ‘{’ s* ‘}’ | ‘return’ e | break | continue | ‘for’ ‘(’ var ‘in’ Array<basic type> ‘)’ ‘{’ s* ‘}’ |‘for’ ‘(’ var ‘in’ var(Int)'..'var(Int) ‘)’ ‘{’ s* ‘}’ | ‘for’ ‘(’ var ‘in’ var(Int)'..'var(Int) step var(Int)‘)’ ‘{’ s* ‘}’ | ‘var’ var = e | ‘val’ var = e | print’(‘var’)’ | println’(‘var’)’ | fun funcName((var: Type)*) { s* }

P :: = s*
```

### Program Entry point: 
Statements.

### Computation Abstraction Non-Trivial Feature
Type inference. var, val can define variable without assigning any supported type for it in Kotlin. I will let it happen in KotlinScript language as well.

### Non-Trivial Feature #2
Semicolon inference. Kotlin support any sentence without semicolon. It means the compiler will figure out where the semicolon is.

### Non-Trivial Feature #3
String interpolation. When print a string, I can put an expression in a string, the string will figure it out what the expression is and print it out. 
For instance, print("$a + $b is ${a + b}"). The string inside print sentence will figure out what a, b and {a + b} are.

### Other Non-Trivial Feature
Comment support in this compiler. You can write comment as you want in your code.
The comment format is the same as java comment.

### Work Planned for Custom Component
I plan to use low-level language JVM bytecode as target language so far.

## How to run it?
Firstly, clone this repository. Then build this project, you just need put the source code file(s) which end with .ks in the project folder.
Then run the main method in Dwks file. Type dwks src.ks to compile source code file, then type java src to run it.
```
dwks *.ks
java *
```
The * means the name of source code file.

There are 3 .ks files in project folder for test compiler. You can test those source code like this.
```
dwks TestBubbleSort.ks
java TestBubbleSort

dwks FindPrimeNumbers.ks
java FindPrimeNumbers

dwks PrintStar.ks
java PrintStar
```
## Running case
![](DwKotlinScriptCompilerRunningEffect.gif)