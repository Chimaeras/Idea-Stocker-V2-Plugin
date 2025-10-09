package com.vermouthx.stocker.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.vermouthx.stocker.StockerAppManager

/**
 * 刷新行情动作
 * 
 * 用户点击工具窗口工具栏的"刷新"按钮时触发。
 * 停止当前的定时任务，清空表格数据，然后重新开始刷新。
 * 
 * 使用场景：
 * 1. 用户手动刷新行情数据
 * 2. 修改设置后重新加载数据
 * 3. 网络恢复后手动刷新
 * 
 * 执行流程：
 * 1. 关闭现有的定时任务
 * 2. 清空表格显示
 * 3. 重新启动定时任务
 * 4. 立即获取一次数据
 * 
 * 注意：
 * - 在后台线程执行（BGT）
 * - 项目为null时禁用按钮
 * 
 * @author VermouthX
 */
class StockerRefreshAction : AnAction() {

    /**
     * 更新动作状态
     * 
     * 在显示工具栏时被调用，决定按钮是否可用。
     * 只有在有效的项目上下文中才启用刷新按钮。
     * 
     * @param e 动作事件
     */
    override fun update(e: AnActionEvent) {
        val project = e.project
        val presentation = e.presentation
        
        // 项目为null时禁用按钮
        if (project == null) {
            presentation.isEnabled = false
        }
    }

    /**
     * 执行刷新动作
     * 
     * 用户点击刷新按钮时被调用。
     * 重启定时任务以获取最新的行情数据。
     * 
     * 执行步骤：
     * 1. shutdownThenClear()：关闭定时任务并清空表格
     * 2. schedule()：重新启动定时任务
     * 
     * @param e 动作事件
     */
    override fun actionPerformed(e: AnActionEvent) {
        // 关闭并清空当前数据
        StockerAppManager.myApplicationMap[e.project]?.shutdownThenClear()
        
        // 重新启动定时刷新任务
        StockerAppManager.myApplicationMap[e.project]?.schedule()
    }

    /**
     * 获取动作更新线程
     * 
     * 返回BGT（Background Thread）表示在后台线程更新动作状态。
     * 避免阻塞UI线程，提升响应性能。
     * 
     * @return 后台线程类型
     */
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
