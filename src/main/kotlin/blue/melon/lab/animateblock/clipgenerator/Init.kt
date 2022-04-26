package blue.melon.lab.animateblock.clipgenerator

import java.io.File
import java.io.FileOutputStream
import java.util.zip.GZIPOutputStream
import javax.imageio.ImageIO
import kotlin.system.exitProcess

class Init {
//    fun File.isImage(): Boolean { //TODO always lowercase by default?
//        return this.extension.lowercase() == "png" || this.extension.lowercase() == "jpg" || this.extension.lowercase() == "bmp"
//    }

    companion object {
        //        private val colorCodes = "1234567890qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM,<.>/?'|;:[{]}-_=+!@#$%^&*()`~".toCharArray()
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size != 5) {
                println("Usage: java -jar ClipGenerator.jar <width:Int> <height:Int> <speed:Double> <outFile:String> <mediaFolder:String>")
                exitProcess(1)
            }
            val targetFile = File(args[3])
            if (targetFile.isDirectory) {
                println("${targetFile.absolutePath} is not a file.")
                exitProcess(1)
            }
            targetFile.createNewFile()
            val targetFileWriter = GZIPOutputStream(FileOutputStream(targetFile))
//            val targetFileWriter = FileOutputStream(targetFile)
            val frameDirectory = File(args[4])
            frameDirectory.mkdir()
            val frames = frameDirectory.listFiles()
            val sortedFrames =
                frames.filter { it.extension.lowercase() in listOf("png", "jpg", "bmp") }.sortedBy { it.name }
            if (sortedFrames.isEmpty()) {
                println("no frame detected.")
                exitProcess(1)
            }
            println("Initialized, ${sortedFrames.size} frames detected.")

            val width = args[0].toInt()
            val height = args[1].toInt()
            val speed = args[2].toDouble()
            var absoluteFrame = 0

            targetFileWriter.write("$width $height\n".toByteArray(Charsets.UTF_8))

            while (true) {
                val frameIndex = (absoluteFrame * speed + 0.5).toInt()
                if (frameIndex > sortedFrames.size - 1) {
                    println("...done!")
                    break
                }

                val frameImage = ImageIO.read(sortedFrames[frameIndex])
                for (z in 0 until height) {
                    for (x in 0 until width) {
                        val xLoc = x / width.toDouble()
                        val yLoc = z / height.toDouble()
                        val color = frameImage.getRGB(
                            ((frameImage.width - 1) * xLoc + 0.5).toInt(),
                            ((frameImage.height - 1) * yLoc + 0.5).toInt()
                        )
                        val colorScheme = ColorScheme.getNearestColorScheme(color)
                        targetFileWriter.write(colorScheme.ordinal + 32)
                    }
                    targetFileWriter.write('\n'.code)
                }
//                targetFileWriter.write("--- END OF FRAME ---\n")
                absoluteFrame++
                print("\r")
                print("$frameIndex / ${sortedFrames.size} frames were proceed (${(frameIndex / sortedFrames.size.toDouble() * 100).toInt()}%)")
            }
            targetFileWriter.finish()
            println("Configuration is generated to ${targetFile.absolutePath}")
            println("${ColorScheme.values().size} colors are supported.")
        }
    }
}