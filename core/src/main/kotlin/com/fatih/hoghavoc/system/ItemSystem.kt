package com.fatih.hoghavoc.system

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.fatih.hoghavoc.component.*
import com.fatih.hoghavoc.events.ExtraLifeEvent
import com.fatih.hoghavoc.events.HealEvent
import com.fatih.hoghavoc.system.AttackFixtureSystem.Companion.BOMB_EXPLODE_FIXTURE
import com.fatih.hoghavoc.utils.BOMB_BIT
import com.fatih.hoghavoc.utils.ITEM_BIT
import com.fatih.hoghavoc.utils.KING_BIT
import com.fatih.hoghavoc.utils.fireEvent
import com.github.quillraven.fleks.*
import ktx.box2d.circle

@AllOf([ItemComponent::class])
class ItemSystem(
    private val itemComps : ComponentMapper<ItemComponent>,
    private val animComps : ComponentMapper<AnimationComponent>,
    private val physicComps : ComponentMapper<PhysicComponent>,
    private val deadComps : ComponentMapper<DeadComponent>,
    private val lifeComps : ComponentMapper<LifeComponent>,
    private val gameStage:Stage
)  : IteratingSystem(interval = Fixed(1/60f)){

    private lateinit var itemComponent: ItemComponent
    private lateinit var animationComponent: AnimationComponent
    private lateinit var physicComponent: PhysicComponent

    override fun onTickEntity(entity: Entity) {

    }

    override fun onAlphaEntity(entity: Entity, alpha: Float) {
        itemComponent = itemComps[entity]
        physicComponent = physicComps[entity]
        animationComponent = animComps[entity]
        itemComponent.run {
            contactTimer -= deltaTime / 2f
            if ((contactTimer <= 0f && collideEntity != null) || itemModel == EntityModel.BOMB){
                when(itemModel){
                    EntityModel.HEARTH->{
                        if (lifeComps[collideEntity!!].extraLife <3){
                            gameStage.fireEvent(ExtraLifeEvent(1))
                            lifeComps[collideEntity!!].extraLife++
                        }
                        configureEntity(entity){
                            deadComps.add(it){
                                destroyInstantly = true
                            }
                        }
                    }
                    EntityModel.BOMB->{
                        animationComponent.run {

                            if (isAnimationDone(AnimationType.ON) && animType == AnimationType.ON){
                                nextAnimation(AnimationType.EXPLODE)
                            }
                            if (animType == AnimationType.EXPLODE && stateTime > 0.2f && physicComponent.body.fixtureList.size <= 1){
                                physicComponent.body.circle(1.5f, Vector2(1.35f,1.35f)){
                                    isSensor = false
                                    density = 500f
                                    restitution = 1.5f
                                    userData = BOMB_EXPLODE_FIXTURE
                                    filter.categoryBits = BOMB_BIT
                                    filter.maskBits = KING_BIT
                                }
                            }
                            if (isAnimationDone(AnimationType.EXPLODE)){
                                configureEntity(entity){
                                    deadComps.add(it){
                                        destroyInstantly = true
                                    }
                                }
                            }
                        }
                    }
                    EntityModel.DIAMOND->{
                        gameStage.fireEvent(HealEvent(extraLife,collideEntity!!))
                        configureEntity(entity){
                            deadComps.add(it){
                                destroyInstantly = true
                            }
                        }
                    }
                    else -> Unit
                }
                collideEntity = null
            }
        }
    }
}
