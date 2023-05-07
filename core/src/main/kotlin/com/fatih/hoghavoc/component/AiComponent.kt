package com.fatih.hoghavoc.component

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ai.btree.BehaviorTree
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeParser
import com.badlogic.gdx.scenes.scene2d.Stage
import com.fatih.hoghavoc.ai.EnemyAiEntity
import com.github.quillraven.fleks.ComponentListener
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World

data class AiComponent(
    val nearbyEntities : MutableSet<Entity> = mutableSetOf(),
    var treePath : String = ""
){
    lateinit var behaviorTree: BehaviorTree<EnemyAiEntity>

    companion object{
        class AiComponentListener(
            private val world : World,
            private val gameStage : Stage
        ) : ComponentListener<AiComponent>{
            private val treeParser = BehaviorTreeParser<EnemyAiEntity>()
            override fun onComponentAdded(entity: Entity, component: AiComponent) {
                component.behaviorTree = treeParser.parse(
                    Gdx.files.internal(component.treePath),
                    EnemyAiEntity(world,entity,gameStage)
                )
            }

            override fun onComponentRemoved(entity: Entity, component: AiComponent) {

            }
        }
    }
}
