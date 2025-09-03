package today.pathos.myapplication.study.agent02.result006

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*

class CommandInvoker(private val scope: CoroutineScope) {
    private val commandQueue = mutableListOf<Command>()
    private val commandHistory = Stack<Command>()
    private var isProcessing = false
    
    fun enqueueCommand(command: Command) {
        commandQueue.add(command)
        processNextCommand()
    }
    
    private fun processNextCommand() {
        if (isProcessing || commandQueue.isEmpty()) return
        
        isProcessing = true
        val command = commandQueue.removeAt(0)
        
        scope.launch {
            try {
                command.execute()
                if (command.canUndo()) {
                    commandHistory.push(command)
                }
            } catch (e: Exception) {
                // Handle command execution error
                e.printStackTrace()
            } finally {
                isProcessing = false
                processNextCommand() // Process next command in queue
            }
        }
    }
    
    fun undoLastCommand() {
        if (commandHistory.isNotEmpty()) {
            val lastCommand = commandHistory.pop()
            scope.launch {
                try {
                    lastCommand.undo()
                } catch (e: Exception) {
                    // Handle undo error
                    e.printStackTrace()
                }
            }
        }
    }
    
    fun hasUndoableCommands(): Boolean = commandHistory.isNotEmpty()
    
    fun getPendingCommandsCount(): Int = commandQueue.size
}