package corp.redacted.game.entity.systems;

import corp.redacted.game.entity.components.CollisionComponent;
import corp.redacted.game.entity.components.StatComponent;
import corp.redacted.game.entity.components.CannonballComponent;
import corp.redacted.game.entity.components.MerchendiseComponent;
import corp.redacted.game.entity.components.TypeComponent;
import corp.redacted.game.entity.components.BodyComponent;
import corp.redacted.game.IConfig;
import corp.redacted.game.WorldBuilder;
import com.badlogic.gdx.math.Vector2;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.math.MathUtils;

import corp.redacted.game.serveur.Socket;


public class CollisionSystem extends IteratingSystem{
  ComponentMapper<CollisionComponent> colM;
  ComponentMapper<StatComponent> boatM;
  ComponentMapper<MerchendiseComponent> merchendiseM;
  ComponentMapper<CannonballComponent> cannonballM;
  ComponentMapper<BodyComponent> bodyMap;
  WorldBuilder world;

  public CollisionSystem(WorldBuilder world) {
    super(Family.all(CollisionComponent.class).get());
    this.colM = ComponentMapper.getFor(CollisionComponent.class);
    this.boatM = ComponentMapper.getFor(StatComponent.class);
    this.merchendiseM = ComponentMapper.getFor(MerchendiseComponent.class);
    this.bodyMap = ComponentMapper.getFor(BodyComponent.class);
    this.cannonballM = ComponentMapper.getFor(CannonballComponent.class);
    this.world = world;
  }

  @Override
  protected void processEntity(Entity entite, float temps){
    CollisionComponent colC = colM.get(entite); //On recupere une collision
    Entity entiteEnCollision = colC.collisionEntite; //On recupere l'entite en collision

    TypeComponent typeEntite = entite.getComponent(TypeComponent.class); // On recupere son type

    /*Consernant les collisions des joueurs (bateau)*/
    if(typeEntite.type == TypeComponent.BATEAU_A){
      BodyComponent bodyA = bodyMap.get(entite);
      StatComponent boat = boatM.get(entite);

      if(entiteEnCollision != null){
        TypeComponent typeEnCollsion = entiteEnCollision.getComponent(TypeComponent.class);
        if(typeEnCollsion != null){
          switch(typeEnCollsion.type){
            case TypeComponent.BATEAU_B :
              /*Mise à jour de la barre de vie du bateau*/
              boat.barreVie -= IConfig.DEGAT_B_B; //Degat de la collision
              colC.collisionEntite = null;
            break;

            case TypeComponent.MARCHANDISE:
              MerchendiseComponent merchendise = merchendiseM.get(entiteEnCollision);
              BodyComponent bodyMerchendise = bodyMap.get(entiteEnCollision);

              // Mise à jour des scores
              boat.point += merchendise.weight;
              //La marchandise disparait de la map
              world.removeEntite(entiteEnCollision);
              for(Fixture fix : bodyMerchendise.body.getFixtureList()){
                bodyMerchendise.body.destroyFixture(fix);
              }
              String message = "points:"+boat.point;
              Socket.envoyerWhiteLists(message, Socket.ROUGE);
              world.creeMarchandise();

              colC.collisionEntite = null;
            break;

            case TypeComponent.CANNONBALL:
              CannonballComponent cannonball = cannonballM.get(entiteEnCollision);
              BodyComponent cannonballB = bodyMap.get(entiteEnCollision);

              if(cannonball.camps != CannonballComponent.BATEAU_A){
                //Mise à jour de la bar de vie
                boat.barreVie -= IConfig.DEGAT_CB_B;
                //Verif si pas de morts
                if(boat.barreVie <= 0){
                  //fin de la manche
                  //world.removeEntite(entite);
                  for(Fixture fix : bodyA.body.getFixtureList()){
                    bodyA.body.destroyFixture(fix);
                  }
                }
                world.removeEntite(entiteEnCollision);
                for(Fixture fix : cannonballB.body.getFixtureList()){
                  cannonballB.body.destroyFixture(fix);
                }
              }
              colC.collisionEntite = null;
            break;

            case TypeComponent.OCEAN:
              Vector2 dirInv = new Vector2(); //Vecteur de direction inverse ;
              float velocity = 9000000f;

              /*Création d'un effet de rebond*/
              float angle = -bodyA.body.getAngle() + (float)(2*Math.PI);
              bodyA.body.setTransform(bodyA.body.getWorldCenter().x, bodyA.body.getWorldCenter().y, angle);

              dirInv.x = MathUtils.cos(bodyA.body.getAngle())*velocity;
              dirInv.y = MathUtils.sin(bodyA.body.getAngle())*velocity;
              dirInv.rotate90(1);
              bodyA.body.applyLinearImpulse(dirInv, bodyA.body.getWorldCenter(), true);

              colC.collisionEntite = null;
            break;

            default:
            break;
          }
        }
      }
    }

    /*Consernant les collisions des joueurs (bateau)*/
    else if(typeEntite.type == TypeComponent.BATEAU_B){
      StatComponent boat = boatM.get(entite);
      BodyComponent bodyB = bodyMap.get(entite);

      if(entiteEnCollision != null){
        TypeComponent typeEnCollsion = entiteEnCollision.getComponent(TypeComponent.class);
        if(typeEnCollsion != null){
          switch(typeEnCollsion.type){
            case TypeComponent.BATEAU_A:
              boat.barreVie -= IConfig.DEGAT_B_B; //Degat de la collision
              colC.collisionEntite = null;
            break;

            case TypeComponent.MARCHANDISE:
              MerchendiseComponent merchendise = merchendiseM.get(entiteEnCollision);
              BodyComponent bodyMerchendise = bodyMap.get(entiteEnCollision);

              // Mise à jour des scores
              boat.point += merchendise.weight;
              //La marchandise disparait de la map
              world.removeEntite(entiteEnCollision);
              for(Fixture fix : bodyMerchendise.body.getFixtureList()){
                bodyMerchendise.body.destroyFixture(fix);
              }
              String message = "points:"+boat.point;
              Socket.envoyerWhiteLists(message, Socket.BLEU);
              world.creeMarchandise();
              colC.collisionEntite = null;
            break;

            case TypeComponent.CANNONBALL:
              CannonballComponent cannonball = cannonballM.get(entiteEnCollision);
              BodyComponent cannonballB = bodyMap.get(entiteEnCollision);

              if(cannonball.camps != CannonballComponent.BATEAU_B){
                //Mise à jour de la bar de vie
                boat.barreVie -= IConfig.DEGAT_CB_B;
                //Verif si pas de morts
                if(boat.barreVie <= 0){
                  //fin de la manche
                  //world.removeEntite(entite);
                  for(Fixture fix : bodyB.body.getFixtureList()){
                    bodyB.body.destroyFixture(fix);
                  }
                }
                world.removeEntite(entiteEnCollision);
                for(Fixture fix : cannonballB.body.getFixtureList()){
                  cannonballB.body.destroyFixture(fix);
                }
              }
              colC.collisionEntite = null;
            break;

            case TypeComponent.OCEAN:
              Vector2 dirInv = new Vector2(); //Vecteur de direction inverse ;
              float velocity = 9000000f;

              /*Création d'un effet de rebond*/
              float angle = -bodyB.body.getAngle() + (float)(2*Math.PI);
              bodyB.body.setTransform(bodyB.body.getWorldCenter().x, bodyB.body.getWorldCenter().y, angle);

              dirInv.x = MathUtils.cos(bodyB.body.getAngle())*velocity;
              dirInv.y = MathUtils.sin(bodyB.body.getAngle())*velocity;
              dirInv.rotate90(1);
              bodyB.body.applyLinearImpulse(dirInv, bodyB.body.getWorldCenter(), true);

              colC.collisionEntite = null;
            break;
            default:
            break;
          }
        }
      }
    }
    else if(typeEntite.type == TypeComponent.CANNONBALL){
      CannonballComponent ball = cannonballM.get(entite);
      BodyComponent bodyCB = bodyMap.get(entite);

      if(entiteEnCollision != null){
        TypeComponent typeEnCollsion = entiteEnCollision.getComponent(TypeComponent.class);
        if(typeEnCollsion != null){
          switch(typeEnCollsion.type){
            case TypeComponent.BATEAU_A:
              if(ball.camps != CannonballComponent.BATEAU_A){
                StatComponent boatA = boatM.get(entiteEnCollision);
                BodyComponent bodyBoatA = bodyMap.get(entiteEnCollision);
                boatA.barreVie -= IConfig.DEGAT_CB_B;
                //Mise à jour de la bar de vie
                //Verif si pas de morts
                if(boatA.barreVie <= 0){
                  //fin de la manche
                  //world.removeEntite(entiteEnCollision);
                  for(Fixture fix : bodyBoatA.body.getFixtureList()){
                    bodyBoatA.body.destroyFixture(fix);
                  }
                }
                //Le boulet disparait
                world.removeEntite(entite);
                for(Fixture fix : bodyCB.body.getFixtureList()){
                  bodyCB.body.destroyFixture(fix);
                }
              }
            colC.collisionEntite = null;
            break;

            case TypeComponent.BATEAU_B:
              if(ball.camps != CannonballComponent.BATEAU_B){
                StatComponent boatB = boatM.get(entiteEnCollision);
                BodyComponent bodyBoatB = bodyMap.get(entiteEnCollision);
                boatB.barreVie -= IConfig.DEGAT_CB_B; //Mise à jour de la bar de vie
                //Verif si pas de morts
                if(boatB.barreVie <= 0){
                  //fin de la manche
                  //world.removeEntite(entiteEnCollision);
                  for(Fixture fix : bodyBoatB.body.getFixtureList()){
                    bodyBoatB.body.destroyFixture(fix);
                  }
                }
                //Le boulet disparait
                world.removeEntite(entite);
                for(Fixture fix : bodyCB.body.getFixtureList()){
                  bodyCB.body.destroyFixture(fix);
                }
              }
           break;

            default:
            break;
          }
        }
      }
    }




  }


}
