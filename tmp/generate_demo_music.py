import math
import os
import struct
import subprocess
import wave

os.makedirs("/app/src/main/assets/music", exist_ok=True)

def generate_track_wav(wav_path, sample_rate, bit_depth, duration_sec, track_type):
    num_samples = int(sample_rate * duration_sec)
    num_channels = 2
    
    # We will generate raw PCM or WAV, then encode with ffmpeg to FLAC with metadata
    with wave.open(wav_path, 'wb') as wav_file:
        wav_file.setnchannels(num_channels)
        wav_file.setsampwidth(bit_depth // 8)
        wav_file.setframerate(sample_rate)
        
        max_amp = (1 << (bit_depth - 1)) - 1
        
        chunk_size = 8192
        samples_written = 0
        
        while samples_written < num_samples:
            current_chunk = min(chunk_size, num_samples - samples_written)
            frames = bytearray()
            
            for i in range(current_chunk):
                t = (samples_written + i) / sample_rate
                
                # Envelope: fade in 2s, fade out 3s
                env = 1.0
                if t < 2.0:
                    env = t / 2.0
                elif t > (duration_sec - 3.0):
                    env = max(0.0, (duration_sec - t) / 3.0)
                
                left = 0.0
                right = 0.0
                
                if track_type == 'synthwave':
                    # Synthwave chords (Cm7 -> Abmaj7 -> Bb -> Gm7)
                    chord_idx = int((t % 16.0) / 4.0)
                    chords = [
                        [130.81, 155.56, 196.00, 233.08, 261.63], # C3, Eb3, G3, Bb3, C4
                        [103.83, 155.56, 207.65, 261.63, 311.13], # Ab2, Eb3, Ab3, C4, Eb4
                        [116.54, 174.61, 233.08, 293.66, 349.23], # Bb2, F3, Bb3, D4, F4
                        [98.00, 146.83, 196.00, 246.94, 293.66]   # G2, D3, G3, B3, D4
                    ]
                    current_chord = chords[chord_idx]
                    bass_freq = current_chord[0]
                    
                    # Bass synth (sawtooth-like via harmonics)
                    bass = 0.4 * math.sin(2 * math.pi * bass_freq * t) + \
                           0.2 * math.sin(2 * math.pi * bass_freq * 2 * t)
                    
                    # Pad
                    pad_l = 0.0
                    pad_r = 0.0
                    for k, f in enumerate(current_chord[1:]):
                        detune_l = math.sin(2 * math.pi * (f - 0.35) * t)
                        detune_r = math.sin(2 * math.pi * (f + 0.35) * t)
                        pad_l += detune_l * (0.15 / (k + 1))
                        pad_r += detune_r * (0.15 / (k + 1))
                    
                    # Arp lead
                    arp_step = int((t * 4.0) % 8)
                    lead_note = current_chord[arp_step % len(current_chord)] * 2.0
                    lead = 0.18 * math.sin(2 * math.pi * lead_note * t) * (1.0 - (t * 4.0 % 1.0) * 0.7)
                    
                    left = (bass * 0.6 + pad_l + lead * 0.8) * 0.6 * env
                    right = (bass * 0.6 + pad_r + lead * 0.5) * 0.6 * env

                elif track_type == 'ambient':
                    # Ethereal floating chords (Dmaj9 -> Gmaj7 -> Bm9 -> Aadd9)
                    chord_idx = int((t % 20.0) / 5.0)
                    chords = [
                        [146.83, 220.00, 277.18, 329.63, 440.00], # D3, A3, C#4, E4, A4
                        [98.00, 196.00, 246.94, 293.66, 392.00],  # G2, G3, B3, D4, G4
                        [123.47, 185.00, 246.94, 293.66, 369.99], # B2, F#3, B3, D4, F#4
                        [110.00, 164.81, 220.00, 277.18, 329.63]  # A2, E3, A3, C#4, E4
                    ]
                    current_chord = chords[chord_idx]
                    
                    l_val = 0.0
                    r_val = 0.0
                    for k, f in enumerate(current_chord):
                        lfo = 1.0 + 0.15 * math.sin(2 * math.pi * (0.1 + k * 0.05) * t)
                        l_val += math.sin(2 * math.pi * f * t) * (0.15 / math.sqrt(k + 1)) * lfo
                        r_val += math.cos(2 * math.pi * (f + 0.2) * t) * (0.15 / math.sqrt(k + 1)) * lfo
                    
                    left = l_val * 0.7 * env
                    right = r_val * 0.7 * env

                else: # 'jazz'
                    # Midnight Jazz (Dm9 -> G13 -> Cmaj9 -> A7alt)
                    chord_idx = int((t % 16.0) / 4.0)
                    chords = [
                        [146.83, 220.00, 261.63, 311.13, 392.00], # D3, A3, C4, Eb4, G4
                        [98.00, 164.81, 246.94, 293.66, 329.63],  # G2, E3, B3, D4, E4
                        [130.81, 196.00, 246.94, 293.66, 392.00], # C3, G3, B3, D4, G4
                        [110.00, 174.61, 220.00, 261.63, 349.23]  # A2, F3, A3, C4, F4
                    ]
                    current_chord = chords[chord_idx]
                    bass_f = current_chord[0]
                    # Walking acoustic bass feel
                    beat = int(t * 2.0) % 4
                    bass_mult = [1.0, 1.25, 1.5, 1.33][beat]
                    current_bass = bass_f * bass_mult
                    bass = 0.35 * math.sin(2 * math.pi * current_bass * t) * (1.0 - (t * 2.0 % 1.0) * 0.4)
                    
                    # EPiano Rhodes tremolo
                    tremolo = 1.0 + 0.25 * math.sin(2 * math.pi * 4.5 * t)
                    keys_l = 0.0
                    keys_r = 0.0
                    for k, f in enumerate(current_chord[1:]):
                        keys_l += math.sin(2 * math.pi * f * t) * 0.12 * tremolo
                        keys_r += math.sin(2 * math.pi * (f + 0.4) * t) * 0.12 * (2.0 - tremolo)
                        
                    left = (bass + keys_l) * 0.7 * env
                    right = (bass * 0.8 + keys_r) * 0.7 * env

                # Clamp
                left = max(-1.0, min(1.0, left))
                right = max(-1.0, min(1.0, right))
                
                if bit_depth == 24:
                    i_left = int(left * 8388607)
                    i_right = int(right * 8388607)
                    frames.extend(struct.pack('<i', i_left)[:3])
                    frames.extend(struct.pack('<i', i_right)[:3])
                else: # 16-bit
                    i_left = int(left * 32767)
                    i_right = int(right * 32767)
                    frames.extend(struct.pack('<hh', i_left, i_right))
                    
            wav_file.writeframes(frames)
            samples_written += current_chunk

# Track configurations
tracks = [
    {
        'filename': 'neon_odyssey.flac',
        'rate': 96000,
        'depth': 24,
        'duration': 42,
        'type': 'synthwave',
        'metadata': [
            '-metadata', 'TITLE=Neon Odyssey',
            '-metadata', 'ARTIST=Musicy Resonance',
            '-metadata', 'ALBUM=Prismatic Horizons',
            '-metadata', 'GENRE=Synthwave Hi-Res',
            '-metadata', 'DATE=2024',
            '-metadata', 'TRACKNUMBER=1',
            '-metadata', 'LYRICS=[00:00.00]Neon Odyssey - Musicy Resonance\n[00:04.00]Electric horizons glowing in the dark\n[00:08.50]Synthesizers pulse beneath the starlight\n[00:13.00]Racing down the cybernetic boulevard\n[00:18.00]Frequencies alive in 96kHz 24-bit\n[00:23.50]Crystal clarity echoing through the night\n[00:29.00]Pure lossless waves guiding the way\n[00:34.00]Neon pulse fades into the twilight'
        ]
    },
    {
        'filename': 'aurora_borealis.flac',
        'rate': 48000,
        'depth': 24,
        'duration': 42,
        'type': 'ambient',
        'metadata': [
            '-metadata', 'TITLE=Aurora Borealis',
            '-metadata', 'ARTIST=Nordic Ensemble',
            '-metadata', 'ALBUM=Echoes of the Fjord',
            '-metadata', 'GENRE=Acoustic Ambient',
            '-metadata', 'DATE=2024',
            '-metadata', 'TRACKNUMBER=2',
            '-metadata', 'LYRICS=[00:00.00]Aurora Borealis - Nordic Ensemble\n[00:05.00]Whispers over frozen mountain waters\n[00:11.00]Emerald ribbons painting the polar sky\n[00:17.50]Gentle resonance breathing through the valley\n[00:24.00]Pure acoustic warmth and tranquil silence\n[00:30.00]Winter solstice dancing above the clouds\n[00:36.00]Echoes resting in serene harmony'
        ]
    },
    {
        'filename': 'velvet_nocturne.flac',
        'rate': 44100,
        'depth': 16,
        'duration': 42,
        'type': 'jazz',
        'metadata': [
            '-metadata', 'TITLE=Velvet Nocturne',
            '-metadata', 'ARTIST=Miles Tribute Quartet',
            '-metadata', 'ALBUM=Blue Room Sessions',
            '-metadata', 'GENRE=Midnight Jazz',
            '-metadata', 'DATE=2024',
            '-metadata', 'TRACKNUMBER=3',
            '-metadata', 'LYRICS=[00:00.00]Velvet Nocturne - Miles Tribute Quartet\n[00:04.50]Rain tapping softly against the cafe window\n[00:09.50]Midnight shadows swaying to the upright bass\n[00:15.00]Velvet chords lingering on the keys\n[00:20.50]A mellow trumpet singing in the dim light\n[00:26.50]Timeless jazz in high fidelity\n[00:32.00]Soft brushes lingering until the morning comes'
        ]
    }
]

for t in tracks:
    wav_path = f"/tmp/{t['filename']}.wav"
    out_flac = f"/app/src/main/assets/music/{t['filename']}"
    print(f"Generating WAV {wav_path}...")
    generate_track_wav(wav_path, t['rate'], t['depth'], t['duration'], t['type'])
    
    cmd = [
        'ffmpeg', '-y', '-i', wav_path,
        *t['metadata'],
        '-c:a', 'flac',
        out_flac
    ]
    print(f"Encoding FLAC {out_flac}...")
    subprocess.check_call(cmd)
    if os.path.exists(wav_path):
        os.remove(wav_path)

print("All bundled FLAC tracks generated successfully!")
