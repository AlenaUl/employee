
package de.hska.employee.config

import java.util.Arrays
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Component

@Aspect
@Component
@Suppress("TooManyFunctions", "unused")
class LogAspect {
    /**
     * Protokollierung der Argumente und des Rückgabewertes einer Methode.
     *
     * @param joinPoint Ein `ProceedingJoinPoint` für _AspectJ_.
     * @return Rückgabewert des protokollierten Methodenaufrufs.
     */
    @Around("execution(* de.hska.*.rest.*Handler.*(..))" +
            " || execution(* de.hska.*.service.*Service*.*(..))" +
            " || execution(* de.hska.*.config.security.*Service*.*(..))")
    @Suppress("UselessPostfixExpression")
    fun logMethod(joinPoint: ProceedingJoinPoint): Any {
        lateinit var result: Any
        with(joinPoint) {
            val clazz = target.javaClass
            val log = LOGGER_MAP.computeIfAbsent(clazz) { getLogger(clazz) }

            if (!log.isDebugEnabled) {
                return proceed()
            }

            val methodName = signature.name

            // Methodenaufruf protokollieren
            logMethodBegin(log, methodName, args)

            // Eigentlicher Methodenaufruf
            result = proceed()

            // Ende der eigentlichen Methode protokollieren
            logMethodEnd(log, methodName, result)
        }
        return result
    }

    @Suppress("NestedBlockDepth")
    private
    fun logMethodBegin(log: Logger, methodName: String, args: Array<Any?>?) {
        var argsStr = ""
        args?.let {
            val sb = StringBuilder(STRING_BUILDER_INITIAL_SIZE)
            val anzahlArgs = args.size
            with(sb) {
                append(": ")
                for (i in 0 until anzahlArgs) {
                    val arg = args[i]
                    val argStr = if (arg == null) "null" else toString(arg)
                    append(argStr)
                    append(PARAM_SEPARATOR)
                }
                val laenge = length
                delete(laenge - PARAM_SEPARATOR_LENGTH, laenge - 1)
            }
            argsStr = sb.toString()
        }
        log.debug("$methodName BEGINN$argsStr")
    }

    private
    fun logMethodEnd(log: Logger, methodName: String, result: Any?) {
        val endStr = if (result == null)
            "$methodName ENDE"
        else "$methodName ENDE: ${toString(result)}"
        log.debug(endStr)
    }

    private
    fun toString(obj: Any): String {
        if (obj is Collection<*>) {
            // die Elemente nur bei kleiner Anzahl ausgeben
            // sonst nur die Anzahl der Elemente
            val anzahl = obj.size
            return if (anzahl > MAX_ELEM) {
                "$COUNT${obj.size}"
            } else obj.toString()
        }

        return if (obj.javaClass.isArray) {
            // Array in String konvertieren: Element fuer Element
            arrayToString(obj)
        } else obj.toString()

        // Objekt, aber keine Collection und kein Array
    }

    private
    fun arrayToString(obj: Any): String {
        val componentClass = obj.javaClass.componentType
        if (!componentClass.isPrimitive) {
            return arrayOfObject(obj)
        }

        // Array von primitiven Werten: byte, short, int, long, float, double,
        // boolean, char
        val className = componentClass.name
        return arrayOfPrimitive(obj, className)
    }

    private
    fun arrayOfObject(obj: Any): String {
        @Suppress("UNCHECKED_CAST")
        val arr = obj as Array<Any>
        return if (arr.size > MAX_ELEM) {
            "$COUNT${arr.size}"
        } else Arrays.toString(arr)
    }

    private
    fun arrayOfPrimitive(obj: Any, className: String) =
            when (className) {
                "byte" -> arrayOfByte(obj)
                "short" -> arrayOfShort(obj)
                "int" -> arrayOfInt(obj)
                "long" -> arrayOfLong(obj)
                "float" -> arrayOfFloat(obj)
                "double" -> arrayOfDouble(obj)
                else -> arrayOfOtherPrimitive(obj, className)
            }

    private
    fun arrayOfOtherPrimitive(obj: Any, className: String) =
            when (className) {
                "boolean" -> arrayOfBoolean(obj)
                "char" -> arrayOfChar(obj)
                else -> "<<UNKNOWN PRIMITIVE ARRAY>>"
            }

    private
    fun arrayOfByte(obj: Any): String {
        val arr = obj as ByteArray
        return if (arr.size > MAX_ELEM) {
            "$COUNT${arr.size}"
        } else Arrays.toString(arr)
    }

    private
    fun arrayOfShort(obj: Any): String {
        val arr = obj as ShortArray
        return if (arr.size > MAX_ELEM) {
            "$COUNT${arr.size}"
        } else Arrays.toString(arr)
    }

    private
    fun arrayOfInt(obj: Any): String {
        val arr = obj as IntArray
        return if (arr.size > MAX_ELEM) {
            "$COUNT${arr.size}"
        } else Arrays.toString(arr)
    }

    private
    fun arrayOfLong(obj: Any): String {
        val arr = obj as LongArray
        return if (arr.size > MAX_ELEM) {
            "$COUNT${arr.size}"
        } else Arrays.toString(arr)
    }

    private
    fun arrayOfFloat(obj: Any): String {
        val arr = obj as FloatArray
        return if (arr.size > MAX_ELEM) {
            "$COUNT${arr.size}"
        } else Arrays.toString(arr)
    }

    private
    fun arrayOfDouble(obj: Any): String {
        val arr = obj as DoubleArray
        return if (arr.size > MAX_ELEM) {
            "$COUNT${arr.size}"
        } else Arrays.toString(arr)
    }

    private
    fun arrayOfBoolean(obj: Any): String {
        val arr = obj as BooleanArray
        return if (arr.size > MAX_ELEM) {
            "$COUNT${arr.size}"
        } else Arrays.toString(arr)
    }

    private
    fun arrayOfChar(obj: Any): String {
        val arr = obj as CharArray
        return if (arr.size > MAX_ELEM) {
            "$COUNT${arr.size}"
        } else Arrays.toString(arr)
    }

    private
    companion object {
        val COUNT = "Anzahl: "
        // bei Collections wird ab 5 Elementen nur die Anzahl ausgegeben
        const val MAX_ELEM = 4

        const val STRING_BUILDER_INITIAL_SIZE = 64
        val PARAM_SEPARATOR = ", "
        val PARAM_SEPARATOR_LENGTH = PARAM_SEPARATOR.length

        val LOGGER_MAP = HashMap<Class<*>, Logger>()
    }
}
