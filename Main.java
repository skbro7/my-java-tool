import React, { useState, useCallback } from 'react';
import { UploadCloud, Image as ImageIcon, Loader, AlertTriangle, Sparkles, Wand2, Download, Gift, BrainCircuit, BookImage, Film } from 'lucide-react';

// --- Main App Component ---
export default function App() {
  const [prompt, setPrompt] = useState("");
  const [uploadedImage, setUploadedImage] = useState(null);
  const [generatedVideo, setGeneratedVideo] = useState(null); // Changed from generatedImage
  const [lastGeneratedPrompt, setLastGeneratedPrompt] = useState(""); // For story continuation
  const [isLoading, setIsLoading] = useState(false);
  const [isSuggesting, setIsSuggesting] = useState(false);
  const [isEnhancing, setIsEnhancing] = useState(false);
  const [isContinuing, setIsContinuing] = useState(false);
  const [error, setError] = useState(null);

  // --- Image Upload Handler (No changes needed) ---
  const handleImageUpload = (e) => {
    const file = e.target.files[0];
    if (file && file.type.startsWith("image/")) {
      const reader = new FileReader();
      reader.onloadend = () => {
        setUploadedImage(reader.result);
        setError(null);
      };
      reader.readAsDataURL(file);
    } else {
        setError("Please upload a valid image file.");
        setUploadedImage(null);
    }
  };

  // --- Drag and Drop Handlers (No changes needed) ---
  const handleDrop = useCallback((e) => {
    e.preventDefault(); e.stopPropagation();
    e.currentTarget.classList.remove('border-indigo-400', 'bg-slate-800/80');
    const file = e.dataTransfer.files[0];
    if (file && file.type.startsWith("image/")) {
        const reader = new FileReader();
        reader.onloadend = () => { setUploadedImage(reader.result); setError(null); };
        reader.readAsDataURL(file);
    } else {
        setError("Please drop a valid image file.");
        setUploadedImage(null);
    }
  }, []);
  const handleDragOver = useCallback((e) => { e.preventDefault(); e.stopPropagation(); }, []);
  const handleDragEnter = useCallback((e) => { e.preventDefault(); e.stopPropagation(); e.currentTarget.classList.add('border-indigo-400', 'bg-slate-800/80'); }, []);
  const handleDragLeave = useCallback((e) => { e.preventDefault(); e.stopPropagation(); e.currentTarget.classList.remove('border-indigo-400', 'bg-slate-800/80'); }, []);

  // --- Unified Gemini Text Generation Function (No changes needed) ---
  const callGemini = async (textPrompt, base64ImageData = null) => {
    // This function for text-based AI assistance remains the same.
    const parts = [{ text: textPrompt }];
    if (base64ImageData) {
        parts.push({ inlineData: { mimeType: "image/jpeg", data: base64ImageData } });
    }
    const payload = { contents: [{ role: "user", parts: parts }] };
    // NOTE: In a real app, API Key should be handled securely on a backend.
    const apiKey = "";
    const apiUrl = `https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=${apiKey}`;

    const response = await fetch(apiUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });

    if (!response.ok) {
        const errorBody = await response.text();
        console.error("Gemini API Error:", errorBody);
        throw new Error("The AI helper failed to respond. Please try again.");
    }
    const result = await response.json();
    if (result.candidates && result.candidates[0]?.content?.parts[0]?.text) {
        return result.candidates[0].content.parts[0].text;
    } else {
        console.error("Unexpected Gemini API response:", result);
        throw new Error("The AI helper returned an unexpected response format.");
    }
  };

  // --- AI Prompt Helper Handlers (No changes needed) ---
  const handleSuggestPrompt = async () => {
      if (!uploadedImage) { setError("Please upload an image first to get suggestions."); return; }
      setIsSuggesting(true); setError(null);
      try {
          const base64ImageData = uploadedImage.split(',')[1];
          const textPrompt = "Analyze this image and generate one creative, highly-detailed prompt for an AI to animate it. Describe a short 3-5 second scene with motion, emotion, and a cinematic style based on the subject. Make it epic and inspiring.";
          setPrompt(await callGemini(textPrompt, base64ImageData));
      } catch (err) { setError(err.message); } finally { setIsSuggesting(false); }
  };
  const handleEnhancePrompt = async () => {
    if (!prompt) { setError("Please write a basic prompt first to enhance it."); return; }
    setIsEnhancing(true); setError(null);
    try {
        const textPrompt = `Take the following user's idea and expand it into a highly detailed, creative, and cinematic prompt for an AI video generator. Add details about camera motion, character expression, lighting changes, and atmosphere to bring the scene to life. Make it epic. Do not use markdown. User's idea: "${prompt}"`;
        setPrompt(await callGemini(textPrompt));
    } catch (err) { setError(err.message); } finally { setIsEnhancing(false); }
  };
  const handleSurpriseMe = async () => {
    setIsSuggesting(true); setError(null);
    try {
        const textPrompt = "Generate one completely random, unique, and highly creative prompt for an AI video generator. The prompt should describe a short, visually stunning, and imaginative 3-5 second clip. Do not use markdown.";
        setPrompt(await callGemini(textPrompt));
    } catch (err) { setError(err.message); } finally { setIsSuggesting(false); }
  };
  const handleContinueStory = async () => {
    if(!lastGeneratedPrompt) { setError("Generate an animation first to continue its story."); return; }
    setIsContinuing(true); setError(null);
    try {
        const textPrompt = `Based on the following prompt that just created an animation, create a new prompt that describes the very next scene in a continuing story. Build upon the previous scene, describing what happens next in a 3-5 second clip. Do not use markdown. Previous prompt: "${lastGeneratedPrompt}"`;
        const nextPrompt = await callGemini(textPrompt);
        setPrompt(nextPrompt);
    } catch(err) {
        setError(err.message);
    } finally {
        setIsContinuing(false);
    }
  };

  // --- ⭐ UPGRADED: AI Animation Generation Handler ---
  const handleGenerateAnimation = async () => {
    if (!prompt) { setError("Please enter a prompt to generate an animation."); return; }
    if (!uploadedImage) { setError("Please upload an image to animate."); return; }

    setIsLoading(true); setError(null); setGeneratedVideo(null);

    try {
        // --- Backend-driven API Call (Highly Recommended) ---
        // In a production app, this entire section would be replaced by a single call
        // to your own backend, which then securely calls the Google Cloud Vertex AI SDK.
        // This is a placeholder demonstrating the required logic for a frontend call.

        const PROJECT_ID = "your-gcp-project-id"; // Replace with your Google Cloud Project ID
        const MODEL_ID = "image-to-video-generation-model"; // e.g., a fine-tuned or specific model like Veo
        const API_ENDPOINT = `https://us-central1-aiplatform.googleapis.com/v1/projects/${PROJECT_ID}/locations/us-central1/publishers/google/models/${MODEL_ID}:predict`;
        const accessToken = ""; // This should be a short-lived OAuth2 token fetched securely.

        const base64ImageData = uploadedImage.split(',')[1];

        const payload = {
            instances: [
                {
                    "prompt": prompt,
                    "image": { "bytesBase64Encoded": base64ImageData }
                }
            ],
            parameters: {
                "duration_seconds": 5,
                "quality": "4k",
                "look": "cinematic",
                "motion_realism": "high"
            }
        };

        const response = await fetch(API_ENDPOINT, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${accessToken}`
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.error?.message || `Animation API request failed with status ${response.status}`);
        }

        const result = await response.json();

        if (result.predictions && result.predictions[0]?.bytesBase64Encoded) {
            setGeneratedVideo(`data:video/mp4;base64,${result.predictions[0].bytesBase64Encoded}`);
            setLastGeneratedPrompt(prompt);
        } else {
            console.error("Unexpected Animation API response:", result);
            throw new Error("The AI did not return a valid animation. Please try a different prompt.");
        }
    } catch (err) {
        setError(err.message);
        // FOR DEMO PURPOSES: Simulate a successful response if the API fails
        console.warn("API call failed. Falling back to demo animation.");
        // You would remove this block in a real application.
        // This simulates a successful return for UI testing.
        setTimeout(() => {
            // Replace with a Base64 encoded MP4 for a real placeholder
            setGeneratedVideo("https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4");
            setLastGeneratedPrompt(prompt);
        }, 2000);

    } finally {
        // We add a delay here to show the loading state even in the demo case
        setTimeout(() => setIsLoading(false), 2000);
    }
  };

  // --- UPGRADED: Download Handler for Video ---
  const handleDownload = () => {
      if (!generatedVideo) return;
      const link = document.createElement('a');
      link.href = generatedVideo;
      link.download = `v3-clasher-animation-${Date.now()}.mp4`; // Changed to .mp4
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
  };

  return (
    <div className="bg-slate-900 min-h-screen flex flex-col items-center justify-center p-4 font-sans text-white">
      <div className="w-full max-w-5xl mx-auto flex flex-col lg:flex-row gap-8">
        
        {/* --- Left Panel (Controls) --- */}
        <div className="lg:w-1/2 flex flex-col gap-6 p-6 bg-slate-800/50 rounded-2xl border border-slate-700 shadow-2xl shadow-indigo-900/20">
          <div className="text-center">
            <h1 className="text-4xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-indigo-400 to-cyan-400 tracking-tight">V3 Clasher</h1>
            <p className="text-slate-400 mt-2">Bring your visual stories to life.</p>
          </div>

          <div className={`relative border-2 border-dashed border-slate-600 rounded-xl p-6 text-center transition-all duration-300 ${uploadedImage ? 'h-64' : 'h-48'} flex flex-col justify-center items-center`}
            onDrop={handleDrop} onDragOver={handleDragOver} onDragEnter={handleDragEnter} onDragLeave={handleDragLeave}>
            <input type="file" id="imageUpload" accept="image/*" onChange={handleImageUpload} className="absolute inset-0 w-full h-full opacity-0 cursor-pointer" />
            {uploadedImage ? (
                <><img src={uploadedImage} alt="Uploaded preview" className="max-h-full h-auto w-auto max-w-full rounded-lg object-contain" /><label htmlFor="imageUpload" className="absolute bottom-2 right-2 text-xs bg-slate-900/80 text-white py-1 px-2 rounded-md cursor-pointer hover:bg-slate-700 transition-colors">Change Image</label></>
            ) : (
                <div className="flex flex-col items-center justify-center gap-2 text-slate-400"><UploadCloud className="w-10 h-10 text-slate-500" /><span className="font-semibold text-slate-300">Click to upload or drag & drop</span><span className="text-sm">PNG, JPG, or WEBP</span></div>
            )}
          </div>
          
          <div>
            <label htmlFor="prompt" className="block text-sm font-medium text-slate-300 mb-2">Animation Prompt</label>
            <textarea id="prompt" placeholder="Describe the motion, style, and emotion..." value={prompt} onChange={e => setPrompt(e.target.value)} rows="4" className="w-full p-3 bg-slate-900/70 border border-slate-700 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-colors text-slate-200 placeholder-slate-500 text-base" />
          </div>
          
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-2">
              <button onClick={handleSuggestPrompt} disabled={isSuggesting || isEnhancing || isContinuing || !uploadedImage} className="flex items-center justify-center gap-1.5 py-2 px-3 text-sm rounded-md bg-slate-700/50 hover:bg-slate-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors">
                  {isSuggesting && !isEnhancing && !isContinuing ? <Loader className="w-4 h-4 animate-spin" /> : <BrainCircuit className="w-4 h-4" />} Suggest ✨
              </button>
              <button onClick={handleEnhancePrompt} disabled={isEnhancing || isSuggesting || isContinuing || !prompt} className="flex items-center justify-center gap-1.5 py-2 px-3 text-sm rounded-md bg-slate-700/50 hover:bg-slate-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors">
                  {isEnhancing ? <Loader className="w-4 h-4 animate-spin" /> : <Wand2 className="w-4 h-4" />} Enhance ✨
              </button>
              <button onClick={handleSurpriseMe} disabled={isSuggesting || isEnhancing || isContinuing} className="flex items-center justify-center gap-1.5 py-2 px-3 text-sm rounded-md bg-slate-700/50 hover:bg-slate-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors">
                  {isSuggesting && !isEnhancing ? <Loader className="w-4 h-4 animate-spin" /> : <Gift className="w-4 h-4" />} Surprise Me ✨
              </button>
          </div>

          <button onClick={handleGenerateAnimation} disabled={isLoading || !prompt || !uploadedImage} className="w-full flex items-center justify-center gap-3 p-4 rounded-lg bg-gradient-to-r from-indigo-500 to-cyan-500 text-white font-bold text-lg shadow-lg hover:shadow-indigo-500/40 transition-all duration-300 transform hover:-translate-y-1 disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none disabled:shadow-none">
            {isLoading ? <><Loader className="w-6 h-6 animate-spin" />Generating...</> : <><Film className="w-6 h-6" />Generate Animation</>}
          </button>
        </div>

        {/* --- Right Panel (Result) --- */}
        <div className="lg:w-1/2 flex flex-col gap-4 p-6 bg-slate-800/50 rounded-2xl border border-slate-700 shadow-2xl shadow-cyan-900/20">
           <div className="flex justify-between items-center"><h2 className="text-2xl font-bold text-slate-200">Result</h2><button onClick={handleDownload} disabled={!generatedVideo || isLoading} className="flex items-center gap-2 py-2 px-3 rounded-lg bg-slate-700/50 hover:bg-slate-700 text-slate-300 font-medium disabled:opacity-50 disabled:cursor-not-allowed transition-colors"><Download className="w-4 h-4" />Download</button></div>
          <div className="w-full aspect-square bg-slate-900 rounded-lg flex items-center justify-center overflow-hidden">
            {isLoading && (<div className="flex flex-col items-center gap-4 text-slate-400"><Loader className="w-12 h-12 animate-spin text-indigo-400" /><span className="text-lg">Animating your vision...</span></div>)}
            {!isLoading && error && (<div className="p-6 flex flex-col items-center gap-4 text-center text-red-400"><AlertTriangle className="w-12 h-12" /><p className="font-semibold">Operation Failed</p><p className="text-sm text-red-300/80">{error}</p></div>)}
            {!isLoading && !error && generatedVideo && (
                <video src={generatedVideo} alt="AI generated animation" className="w-full h-full object-contain" autoPlay loop muted playsInline />
            )}
            {!isLoading && !error && !generatedVideo && (<div className="flex flex-col items-center gap-4 text-slate-500"><ImageIcon className="w-16 h-16" /><span className="text-lg">Your generated animation will appear here</span></div>)}
          </div>
          <div>
            <button onClick={handleContinueStory} disabled={!generatedVideo || isContinuing || isLoading} className="w-full flex items-center justify-center gap-2 py-3 px-4 text-base rounded-md bg-teal-600/50 hover:bg-teal-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors font-semibold">
               {isContinuing ? <Loader className="w-5 h-5 animate-spin" /> : <BookImage className="w-5 h-5" />} Continue the Story ✨
            </button>
          </div>
        </div>
      </div>
    </div>
  );
  }
