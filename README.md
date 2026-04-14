# Práctica 3: FocusBuddy (Técnica Pomodoro)

**Nombre:** Joshua Abel Hurtado Aponte  
**No. de Cuenta:** 320176900

## Descripción general
Esta es una app de productividad basada en Pomodoro. Me basé en el esqueleto base del laboratorio y le fui armando toda la lógica para que el cronómetro funcionara con sus ciclos

Las tareas que hice fueron:
- Separar el código usando la arquitectura MVC
- Quitar absolutamente todo el hardcoding de los XML y de Java para meterle internacionalización (app en Inglés y Español)
- Implementar una base de datos local con SQLite para guardar el historial de las sesiones 
- Usar SharedPreferences para guardar el idioma y el tema (claro/oscuro)
- Bloquear la pantalla en vertical desde el Manifest para que el reloj no falle si giras el celular
- Darle un diseño propio (TV Girl) y hacer que el celular vibre al terminar

## ¿Qué me costó más trabajo?
La internacionalización y el cambio de idioma desde la app. Pasar los textos al strings.xml fue la parte fácil, pero hacer que al elegir Inglés en Preferencias TODA la app se recargara y guardara sin crashear, me costó. También me tomó un rato entender cómo configurar bien los Chips para que el filtrado del historial funcionara bien

## Si hiciera una segunda versión (V2), ¿qué le agregaría?
1. **Tiempos personalizados:** Que el usuario pueda decidir si quiere estudiar 45 minutos y descansar 10, en lugar de estar con los 25 fijos
2. **Gráficas:** En lugar de solo ver la lista de sesiones, estaría bien una gráfica de barras para ver qué día de la semana fuiste más productivo
3. **Alarmas sonoras:** En este momento vibra y saca un mensaje, pero estaría mejor que sonara una alarma o te dejara elegir tu propio tono para cuando acabe el tiempo.
