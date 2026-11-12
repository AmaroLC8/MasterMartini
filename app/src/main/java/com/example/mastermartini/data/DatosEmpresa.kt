package com.example.mastermartini.data

// DATOS FIJOS DE LA EMPRESA
// "object" = existe UNA sola copia en toda la app (no se crea con "DatosEmpresa()").
// Se usa directo: DatosEmpresa.URL_CATALOGO.
// "const val" = constante que se conoce al compilar y nunca cambia.
// Se escribe aquí (y no en cada pantalla) para que, si cambia la dirección,
// se corrija en un solo lugar.
object DatosEmpresa {

    // Catálogo público de Panadería & Pastelería de Master Martini Chile,
    // publicado en su sitio web (archivo PDF). Se abre en el navegador del celular.
    const val URL_CATALOGO =
        "https://mastermartini.cl/wp-content/uploads/2023/07/Panaderia-Pasteleria-Catalogo.pdf"

    // CONTACTO CON VENTAS (lo usa ui/components/ContactoVentas.kt)
    // DATOS DE EJEMPLO. No poner números ni correos reales hasta que Master Martini lo autorice.
    // example.com es un dominio reservado para ejemplos.

    // Número de WhatsApp con código de país y sin "+" ni espacios (ej. "56912345678").
    // Vacío = se abre WhatsApp con el mensaje y el usuario elige a quién enviarlo.
    const val WHATSAPP_VENTAS = ""

    // Teléfono de ventas (ej. "+56221234567"). Vacío = se abre el marcador sin número.
    const val TELEFONO_VENTAS = ""

    // Correo de ventas
    const val CORREO_VENTAS = "ventas@example.com"

    // PRECIO DEL PLAN PRO (suscripción mensual que desbloquea los cursos intermedios y avanzados).
    // DATOS DE EJEMPLO. No es un precio real de Master Martini hasta que la empresa lo defina.
    // Va en pesos chilenos al mes y como Int (número entero): el peso chileno no usa decimales.
    // Lo usa CapacitacionesViewModel, que lo convierte en el texto "$9.990".
    const val PRECIO_PLAN_PRO = 9990
}
