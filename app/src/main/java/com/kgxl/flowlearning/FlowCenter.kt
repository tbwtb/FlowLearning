package com.kgxl.flowlearning

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

/**
 *  Created by zjy on 2021/7/20.
 */

fun main(args: Array<String>) {
    //run method start
    onStart()
}

fun simpleDelay(): Flow<Int> = flow {
    println("start flow")
    for (i in 1..5) {
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
 * 与[Flow.map]相比唯一区别为返回值.[mapOperator]
 * 与[Flow.flatMapMerge]相比它是按照输出顺序输出.[flatmapMergeOperator]
 * 例如：
 *  flow 1: 1,2,3
 *  flow 2: 6,7,8
 *  out :
 *      1->6
 *      1->7
 *      1->8
 *      2->6
 *      2->7
 *      2->8
 *      3->6
 *      3->7
 *      3->8
 *
 */
fun flatmapOperator() {
    runBlocking {
        simple().flatMapConcat {
            //输出的值为flow<R>类型
            println("flatMapConcat = $it")
            flowOf(1, 4).map { cur -> "$it -> $cur" }
        }.collect {
            println("collect = $it")
        }
    }
}

/**
 * 是全部先执行完输入再执行输出
 *  例如：
 *  flow 1:1,2,3
 *  flow 2:6,7,8
 *  out :
 *      1->6
 *      2->7
 *      3->8
 *      4->9
 *      5->10
 */
fun flatmapMergeOperator() {
    runBlocking {
        simple().flatMapMerge {
            //输出的值为flow<R>类型
            println("flatMapMerge = $it")
            flowOf(4, 5).map { cur -> "$cur $it" }
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

/**
 * 指定条件过滤输入值
 */
fun filterOperator() {
    runBlocking {
        simple().filter {
            it > 3
        }.collect {
            println("collect = $it")
        }
    }
}

fun withContext() {

    runBlocking {
        flow {
            for (i in 1..3) {
                Thread.sleep(100)
                emit(i)
            }
        }.flowOn(Dispatchers.Default).collect {
            println("collect = $it")
        }
    }

    runBlocking {
        flow {
            // The WRONG way to change context for CPU-consuming code in flow builder
            kotlinx.coroutines.withContext(Dispatchers.Default) {
                for (i in 1..3) {
                    Thread.sleep(100) // pretend we are computing it in CPU-consuming way
                    emit(i) // emit next value
                }
            }
        }.collect {
            println("collect = $it")
        }
    }
}

/**
 * 在输入流时当第一个进入，缓存，直接发送，无需挨个等待
 * 不使用buffer 顺序执行，使用buffer会先并行
 *  结果如下：
 *       map1 1
 *       collect = 1
 *       map1 2
 *       collect = 2
 *       map1 3
 *       collect = 3
 *       map1 4
 *       collect = 4
 *       map1 5
 *       collect = 5
 *
 *       map1 buffer 1
 *       map1 buffer 2
 *       map1 buffer 3
 *       map1 buffer 4
 *       map1 buffer 5
 *       collect buffer = 1
 *       collect buffer = 2
 *       collect buffer = 3
 *       collect buffer = 4
 *       collect buffer = 5
 */
fun bufferOperator() {
    runBlocking {
        simple().map {
            println("map1 $it")
            it
        }.collect { value ->
            println("collect = $value")
        }
    }

    runBlocking {
        simple().map {
            println("map1 buffer $it")
            it
        }.buffer().collect { value ->
            println("collect buffer = $value")
        }
    }
}

/**
 * 取最新的
 * 当上游的发送速度小于处理的速度。只会取最新的一个
 */
fun collectLatest() {
    runBlocking {
        simpleDelay().collectLatest {
            println("collectLatest delay before= $it")
            delay(250)
            println("collectLatest delay after= $it")
        }
    }
}

/**
 * 两个flow合并成一个新的flow
 */
fun zipOperator() {
    runBlocking {
        (1..5).asFlow().zip((5..10).asFlow()) { a, b ->
            a + b
        }.collect {
            println("collect = $it")
        }
    }
}

/**
 * catch flow中的错误作统一处理
 * 注意此处的顺序,catch只能catch到之前操作的错误，之后的操作并不会走catch
 */
fun catch() {
    runBlocking {
        (1..5).asFlow().catch { e ->
            println(e.message.toString())
        }.map {
            if (it > 3) {
                throw IllegalStateException("number to large")
            }
            it
        }.collect {
            check(it > 3) { "number to large" }
        }
    }
}

/**
 * 不管是否有异常，只要结束都会执行[onCompletion]
 * 此方法在catch之后，一旦流有异常，此处是得不到异常的，被catch住了。
 * 所以不要再此处处理异常.
 */
fun onComplete() {
    runBlocking {
        simple().map {
            if (it > 3) {
                throw IllegalStateException("number to large")
            }
            it
        }.onCompletion { e ->
            println("onCompletion : ${e?.message.toString()}")
        }.catch { e ->
            println("catch : ${e.message.toString()}")
        }.collect {
            println("collect = $it")
        }
    }
}

/**
 * [Flow.onStart]在流开始的时候会执行
 */
fun onStart() {
    runBlocking {
        simple().onStart {
            println("start")
        }.onCompletion { e ->
            println("onCompletion : ${e?.message.toString()}")
        }.collect {
            println("collect = $it")
        }
    }
}
