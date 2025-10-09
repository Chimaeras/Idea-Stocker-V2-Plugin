package com.vermouthx.stocker.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.vermouthx.stocker.StockerAppManager

/**
 * 停止刷新动作
 * 
 * 用户点击工具窗口工具栏的"停止"按钮时触发。
 * 停止定时刷新任务，但保留表格数据（不清空）。
 * 
 * 使用场景：
 * 1. 用户临时不需要刷新行情（但想保留当前数据）
 * 2. 节省网络带宽和CPU资源
 * 3. 停止频繁的API请求
 * 
 * 执行流程：
 * 1. 关闭定时任务
 * 2. 保留当前表格数据
 * 3. 释放线程资源
 * 
 * 与刷新的区别：
 * - 停止：关闭任务，保留数据
 * - 刷新：关闭任务+清空+重启
 * 
 * 恢复方法：
 * - 点击"刷新"按钮重新启动
 * 
 * @author VermouthX
 */
class StockerStopAction : AnAction() {
    
    /**
     * 更新动作状态
     * 
     * 决定停止按钮是否可用。
     * 
     * 禁用条件：
     * 1. 项目为null
     * 2. 应用已经停止（避免重复停止）
     * 
     * @param e 动作事件
     */
    override fun update(e: AnActionEvent) {
        val project = e.project
        val presentation = e.presentation
        
        // 项目为null时禁用
        if (project == null) {
            presentation.isEnabled = false
        }
        
        // 应用已停止时禁用（避免重复停止）
        val myApplication = StockerAppManager.myApplication(project)
        if (myApplication?.isShutdown() == true) {
            presentation.isEnabled = false
        }
    }

    /**
     * 执行停止动作
     * 
     * 用户点击停止按钮时被调用。
     * 只关闭定时任务，不清空数据。
     * 
     * @param e 动作事件
     */
    override fun actionPerformed(e: AnActionEvent) {
        val myApplication = StockerAppManager.myApplication(e.project)
        // 关闭定时任务（保留数据）
        myApplication?.shutdown()
    }

    /**
     * 获取动作更新线程
     * 
     * 返回BGT（Background Thread）表示在后台线程更新动作状态。
     * 
     * @return 后台线程类型
     */
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
