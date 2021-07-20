package com.kgxl.flowlearning

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.lang.Exception

/**
 *  Created by zjy on 2021/7/20.
 */
class FlowCenter {

}

fun simpleDelay(): Flow<Int> = flow {
    println("start flow")
    for (i in 1..3) {
        delay(200)
        emit(i)
    }
    println("end flow")
}

fun simple(): Flow<Int> = flow {
    for (i in 1..5) {
        emit(i)
    }
}

fun main(args: Array<String>) {
    reduceOperator()
}

/**
 * 只有对flow开始收集时,才会执行flow内部,就是所谓的流是冷的
 */
fun collect() {
    runBlocking {
        val simple = simpleDelay()
        println("start sleep")
        delay(500)
        println("end sleep")
        println("start collect")
        simple.collect {
            println("collect = $it")
        }
        println("start collect")
    }
}

/**
 * 设置内部超时时间,内部超过设置时间时抛出TimeoutCancelException
 */
fun withTimeOut() {
    runBlocking {
        withTimeout(500) {
            simpleDelay().collect {
                println("collect = $it")
            }
        }
    }
}

/**
 * 构建flow方式
 */
fun buildFlow() {
    runBlocking {
        (1..10).asFlow().collect { }
        flowOf(1..10).collect { }
        flow {
            emit(1..10)
        }.collect { }
    }
}

/**
 * [Flow.map]操作符
 *  可以改变传入的值
 */
fun mapOperator() {
    runBlocking {
        simple().map {
            //这里可以对传入的值做操作
            "$it 1111"
        }.collect {
            println("collect = $it")
        }
    }
}

/**
 * [Flow.flatMapConcat]操作符
 * 可以改变传入的值，但返回值必须为flow<R>
 * 与[Flow.map]相比唯一区别为返回值
 */
fun flatmapOperator() {
    runBlocking {
        simple().flatMapConcat {
            //输出的值为flow<R>类型
            println("flatMapConcat = $it")
            simple()
        }.collect {
            println("collect = $it")
        }
    }
}

/**
 * 类似[Flow.map],但是更灵活可以返回多个不同类型
 * [Flow.map]只是在它基础上emit了一次而已
 */
fun transformOperator() {
    runBlocking {
        val transform = simple().transform {
            emit("$it transform")
            emit(1)
        }
        transform.collect {
            println("collect = $it")
        }
    }
}

/**
 * [Flow.take]根据flow内部调用的次数判断后续是否还要继续发送，超过是会抛出[kotlinx.coroutines.flow.internal.AbortFlowException]
 */
fun takeOperator() {
    runBlocking {
        val flow = flow {
            try {
                emit(1)
                emit(2)
                emit(3)
                emit(4)
            } catch (e: Exception) {
                println("e = ${e.message.toString()}")
            } finally {
                println("finally emit end")
            }
        }
        flow.take(2).collect {
            println("collect = $it")
        }
    }
}

/**
 * [Flow.reduce]累加操作符
 */
fun reduceOperator() {
    runBlocking {
        val sum = simple().reduce { a, b ->
            a + b
        }
        println("sum = $sum")
    }
}