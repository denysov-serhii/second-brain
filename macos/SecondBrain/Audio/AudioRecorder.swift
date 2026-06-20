import AVFoundation
import Foundation
import Observation

enum AudioRecorderState {
    case idle
    case recording
    case stopped(URL)
    case error(String)
}

@Observable
final class AudioRecorder {
    private(set) var state: AudioRecorderState = .idle
    private var recorder: AVAudioRecorder?

    func startRecording() {
        let tempDir = FileManager.default.temporaryDirectory
        let fileName = "audio_\(Int(Date().timeIntervalSince1970)).m4a"
        let fileURL = tempDir.appendingPathComponent(fileName)

        let settings: [String: Any] = [
            AVFormatIDKey: Int(kAudioFormatMPEG4AAC),
            AVSampleRateKey: 44100,
            AVNumberOfChannelsKey: 1,
            AVEncoderAudioQualityKey: AVAudioQuality.high.rawValue
        ]

        do {
            recorder = try AVAudioRecorder(url: fileURL, settings: settings)
            recorder?.record()
            state = .recording
        } catch {
            state = .error("Cannot start recording: \(error.localizedDescription)")
        }
    }

    func stopRecording() {
        guard let recorder else { return }
        recorder.stop()
        let url = recorder.url
        self.recorder = nil
        state = .stopped(url)
    }

    func discard() {
        recorder?.stop()
        if case .stopped(let url) = state {
            try? FileManager.default.removeItem(at: url)
        }
        recorder = nil
        state = .idle
    }
}
