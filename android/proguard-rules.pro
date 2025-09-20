# Keep the application classes
-keep class com.example.forestcollect.** { *; }

# LibGDX native method registrations
-keep class com.badlogic.gdx.physics.box2d.World { *; }
-keepclassmembers class ** {
    @com.badlogic.gdx.utils.GdxRuntimeException *;
}
