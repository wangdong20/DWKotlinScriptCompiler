# MyKotlinScriptCompiler
This is Kotlin like language compiler that I try to write in Comp430 class.

##Language name
DWKotlinScript

##Compiler Implementation Language and Reasoning
Java. I am familiar with Java and Java is compatible with Kotlin which is good to write my own version Kotlin like language by Java.

##Target Language
JVM bytecode(If it is too hard, I will use Java as target language).

##Language Description
Kotlin is a cross-platform, statically typed, general-purpose programming language with type inference. Kotlin is designed to interoperate fully with Java, and the JVM version of its standard library depends on the Java Class Library, but type inference allows its syntax to be more concise. In 2019 Google made Kotlin as the first language in Android development, I can see the benefit of using Kotlin language from Google’s announcement.

##Planned Restrictions
Kotlin is a power object oriented language. But I will make some limitation on my own Kotlin like language due to the lack of time and energy and the limitation of my own knowledge on Kotlin compiler. There will be no object in KotlinScript, in other word, no class in KotlinScript. Only support basic number type such as Int currently. I will also support String type, because String type is important in Kotlin. Array is also supported but only with basic generic type. It will support high order function, type inference. No garbage collection.

##Syntax
* var is variable
* fn is function name
* e is expression
* s is statement
* op is operator
* T is type variable

Basic type::= Int | String | Boolean | Unit

type :: = Int | Boolean| String | Array<basic type> | MutableList<basic type> | ‘(‘type*’)’ -> type | T

e :: = ‘(‘ e ’)’ | e1 binop e2 | unop e |  fn’(‘e’)’ | var | arrayOf(var*) | arrayOf(e1(Int), e2((Int) -> basic type)) | mutableListOf<var>’(‘’)’ | var = e |‘{‘ var : type* -> e(return basic type) ‘}’

binop :: = ‘-’ | ‘+’ | ‘/’ | ‘*’ | ‘%’ | ‘||’ | ‘&&’ | ‘<’ | ‘>’ | ‘<=’ | ‘>=’ | ‘==’ | ‘!=’ | ‘+=’ | ‘-=’

unop :: = ‘!’ | ‘++’ | ‘--’

s :: = ‘if’ ‘(’ e ‘)’ ‘{‘ s* ‘}’ ‘else’ ‘{‘ s* ‘}’ | ‘while’ ‘(’ e ‘)’ ‘{’ s* ‘}’ | ‘return’ e | break | continue | ‘for’ ‘(’ var ‘in’ Array<basic type> ‘)’ ‘{’ s* ‘}’ | ‘var’ var = e | ‘val’ var = e | print’(‘var’)’ | println’(‘var’)’

##Computation Abstraction Non-Trivial Feature
Higher-order functions, which is the lambda expression in Kotlin, I will support lambda expression in my KotlinScript language.

##Non-Trivial Feature #2
Type inference. var, val can define variable without assigning any supported type for it in Kotlin. I will let it happen in KotlinScript language as well.

##Non-Trivial Feature #3
Semicolon inference. Kotlin support any sentence without semicolon. It means the compiler will figure out where the semicolon is.

##Work Planned for Custom Component
I plan to use low-level language JVM bytecode as target language so far.