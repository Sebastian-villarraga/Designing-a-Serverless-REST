# Predicción previa a DELETE

Esta es una propuesta del agente para que el estudiante la evalúe; no se presenta como una predicción personal del estudiante.

- OrderService incorporará deleteOrder(id), con validación y error si no existe.
- El adaptador proxy traducirá el resultado a 204 sin cuerpo o 404 JSON.
- El handler custom incorporará una operación DELETE; Gateway mapeará su resultado.
- No hace falta otra Lambda: las funciones existentes pueden despachar esta operación.
- Ambas configuraciones de API Gateway necesitan la ruta DELETE /orders/{id}.
- Se probará borrado existente, consulta posterior, segundo borrado, id inválido y cuerpo vacío del 204.
- La eliminación corresponde al servicio Java; la decisión HTTP corresponde al adaptador o a las plantillas Gateway.
