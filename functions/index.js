const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { defineSecret } = require("firebase-functions/params");

const OpenAI = require("openai");

const OPENAI_API_KEY = defineSecret("OPENAI_API_KEY");

exports.preguntarIA = onCall(
  {
    secrets: [OPENAI_API_KEY],
  },

  async (request) => {

    // Verificar usuario logueado
    if (!request.auth) {

      throw new HttpsError(
        "unauthenticated",
        "Debes iniciar sesión."
      );
    }

    const pregunta = request.data.pregunta;

    // Validar pregunta
    if (!pregunta) {

      throw new HttpsError(
        "invalid-argument",
        "La pregunta es obligatoria."
      );
    }

    // Cliente OpenAI
    const client = new OpenAI({
      apiKey: OPENAI_API_KEY.value(),
    });

    // Preguntar a la IA
    const response = await client.responses.create({

      model: "gpt-4.1-mini",

      input: `
Eres el asistente inteligente de GeoEduAR.

La app ayuda a localizar Ingenieros de Sistemas usando:
- GPS
- ARCore
- perfiles académicos

Responde de forma:
- clara
- corta
- profesional

Pregunta del usuario:
${pregunta}
      `
    });

    // Respuesta
    return {
      respuesta: response.output_text,
    };
  }
);