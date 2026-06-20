import SwiftUI
import SwiftData

struct NewLogView: View {
    @Environment(\.modelContext) private var modelContext
    @State private var viewModel = NewLogViewModel()
    @State private var audioRecorder = AudioRecorder()
    @State private var selectedTab = 0
    @State private var textInput = ""

    var body: some View {
        VStack(spacing: 0) {
            Picker("Input type", selection: $selectedTab) {
                Text("Text").tag(0)
                Text("Audio").tag(1)
            }
            .pickerStyle(.segmented)
            .padding()

            if selectedTab == 0 {
                textInputSection
            } else {
                audioInputSection
            }

            Spacer()
        }
        .onAppear {
            viewModel.configure(modelContext: modelContext)
        }
        .onChange(of: viewModel.saveState) { _, newState in
            if case .success = newState {
                textInput = ""
                audioRecorder.discard()
                viewModel.resetState()
            }
        }
        .navigationTitle("New Entry")
    }

    @ViewBuilder
    private var textInputSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            TextEditor(text: $textInput)
                .font(.body)
                .frame(minHeight: 180)
                .border(Color.secondary.opacity(0.3))

            if case .error(let msg) = viewModel.saveState {
                Text(msg).foregroundStyle(.red).font(.caption)
            }

            Button(action: {
                Task { await viewModel.saveTextLog(content: textInput) }
            }) {
                if case .saving = viewModel.saveState {
                    ProgressView().controlSize(.small)
                } else {
                    Text("Save")
                        .frame(maxWidth: .infinity)
                }
            }
            .disabled(textInput.trimmingCharacters(in: .whitespaces).isEmpty || isSaving)
        }
        .padding()
    }

    @ViewBuilder
    private var audioInputSection: some View {
        VStack(spacing: 20) {
            Spacer()

            switch audioRecorder.state {
            case .idle:
                VStack(spacing: 12) {
                    Image(systemName: "mic.circle")
                        .font(.system(size: 56))
                        .foregroundStyle(.secondary)
                    Text("Tap to start recording")
                        .foregroundStyle(.secondary)
                    Button("Start Recording") {
                        audioRecorder.startRecording()
                    }
                    .controlSize(.large)
                }

            case .recording:
                VStack(spacing: 12) {
                    Image(systemName: "mic.fill")
                        .font(.system(size: 56))
                        .foregroundStyle(.red)
                        .symbolEffect(.pulse)
                    Text("Recording…")
                        .foregroundStyle(.red)
                    Button("Stop Recording") {
                        audioRecorder.stopRecording()
                    }
                    .controlSize(.large)
                }

            case .stopped(let url):
                VStack(spacing: 12) {
                    Image(systemName: "checkmark.circle")
                        .font(.system(size: 56))
                        .foregroundStyle(.green)
                    Text("Recording ready")

                    if case .error(let msg) = viewModel.saveState {
                        Text(msg).foregroundStyle(.red).font(.caption)
                    }

                    HStack(spacing: 12) {
                        Button("Discard") {
                            audioRecorder.discard()
                        }
                        .foregroundStyle(.red)

                        Button(action: {
                            Task { await viewModel.saveAudioLog(fileUrl: url) }
                        }) {
                            if case .saving = viewModel.saveState {
                                ProgressView().controlSize(.small)
                            } else {
                                Text("Save Recording")
                            }
                        }
                        .disabled(isSaving)
                        .buttonStyle(.borderedProminent)
                    }
                }

            case .error(let msg):
                VStack(spacing: 8) {
                    Image(systemName: "exclamationmark.triangle")
                        .font(.system(size: 40))
                        .foregroundStyle(.orange)
                    Text(msg).foregroundStyle(.secondary)
                    Button("Try Again") { audioRecorder.discard() }
                }
            }

            Spacer()
        }
        .padding()
    }

    private var isSaving: Bool {
        if case .saving = viewModel.saveState { return true }
        return false
    }
}
